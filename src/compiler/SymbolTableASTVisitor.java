package compiler;

import java.util.*;
import compiler.AST.*;
import compiler.exc.*;
import compiler.lib.*;

public class SymbolTableASTVisitor extends BaseASTVisitor<Void,VoidException> {
	
	private List<Map<String, STentry>> symTable = new ArrayList<>();
	Map<String, Map<String, STentry>> classTable = new HashMap<>();//mappa ogni nome di classe nella propria virtual table. Preserviamo campi e metodi
	private int nestingLevel=0; // current nesting level
	private int decOffset=-2; // counter for offset of local declarations at current nesting level. before there is the return address
	int stErrors=0;
	Set<String> onClassVisitScope; //Rende possibile rilevare la ridefinizione(erronea) di campi e metodi con stesso nome effettuata all'interno della stessa classe

	SymbolTableASTVisitor() {}
	SymbolTableASTVisitor(boolean debug) {super(debug);} // enables print for debugging

	private STentry stLookup(String id) {
		int j = nestingLevel;
		STentry entry = null;
		while (j >= 0 && entry == null) 
			entry = symTable.get(j--).get(id);	
		return entry;
	}

	@Override
	public Void visitNode(ProgLetInNode n) {
		if (print) printNode(n);
		Map<String, STentry> hm = new HashMap<>();
		symTable.add(hm);
	    for (Node dec : n.declist) visit(dec);
		visit(n.exp);
		symTable.remove(0);
		return null;
	}

	@Override
	public Void visitNode(ProgNode n) {
		if (print) printNode(n);
		visit(n.exp);
		return null;
	}
	//l'offset delle funzioni sarà come quello delle variabili e quindi bisogna decementarlo mentre invece per i paramenti andremo a incrementarlo
	@Override
	public Void visitNode(FunNode n) {
		if (print) printNode(n);
		Map<String, STentry> hm = symTable.get(nestingLevel);
		List<TypeNode> parTypes = new ArrayList<>();  
		for (ParNode par : n.parlist) parTypes.add(par.getType()); 
		STentry entry = new STentry(nestingLevel, new ArrowTypeNode(parTypes,n.retType),decOffset--);
		//inserimento di ID nella symtable
		if (hm.put(n.id, entry) != null) {
			System.out.println("Fun id " + n.id + " at line "+ n.getLine() +" already declared");
			stErrors++;
		} 
		//creare una nuova hashmap per la symTable
		nestingLevel++;
		Map<String, STentry> hmn = new HashMap<>();
		symTable.add(hmn);
		int prevNLDecOffset=decOffset; // stores counter for offset of declarations at previous nesting level 
		decOffset=-2; //lo resetto perché entro in un nuovo livello. dopo devo riprendere il precedente quando esco dallo scope
		
		int parOffset=1;
		for (ParNode par : n.parlist)
			if (hmn.put(par.id, new STentry(nestingLevel,par.getType(),parOffset++)) != null) {
				System.out.println("Par id " + par.id + " at line "+ n.getLine() +" already declared");
				stErrors++;
			}
		for (Node dec : n.declist) visit(dec);
		visit(n.exp);
		//rimuovere la hashmap corrente poiche' esco dallo scope               
		symTable.remove(nestingLevel--);
		decOffset=prevNLDecOffset; // restores counter for offset of declarations at previous nesting level. lo faccio perché sono uscito dallo scope in cui ero e quindi riprendo lo scope precedente
		return null;
	}
	
	@Override
	public Void visitNode(VarNode n) {
		if (print) printNode(n);
		visit(n.exp);
		Map<String, STentry> hm = symTable.get(nestingLevel);
		STentry entry = new STentry(nestingLevel,n.getType(),decOffset--);
		//inserimento di ID nella symtable
		if (hm.put(n.id, entry) != null) {
			System.out.println("Var id " + n.id + " at line "+ n.getLine() +" already declared");
			stErrors++;
		}
		return null;
	}

	@Override
	public Void visitNode(PrintNode n) {
		if (print) printNode(n);
		visit(n.exp);
		return null;
	}

	@Override
	public Void visitNode(IfNode n) {
		if (print) printNode(n);
		visit(n.cond);
		visit(n.th);
		visit(n.el);
		return null;
	}
	
	@Override
	public Void visitNode(EqualNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(LessEqualNode n) {
		if (print) {
			printNode(n);
		}
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(GreaterEqualNode n) {
		if (print) {
			printNode(n);
		}
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(AndNode n) {
		if (print) {
			printNode(n);
		}
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(OrNode n) {
		if (print) {
			printNode(n);
		}
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(NotNode n) {
		if (print) {
			printNode(n);
		}
		visit(n.exp);
		return null;
	}

	@Override
	public Void visitNode(TimesNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(DivNode n) {
		if (print) {
			printNode(n);
		}
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(PlusNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(MinusNode n) {
		if (print) {
			printNode(n);
		}
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(CallNode n) {
		if (print) printNode(n);
		STentry entry = stLookup(n.id);
		if (entry == null) {
			System.out.println("Fun id " + n.id + " at line "+ n.getLine() + " not declared");
			stErrors++;
		} else {
			n.entry = entry;
			n.nl = nestingLevel;
		}
		for (Node arg : n.arglist) visit(arg);
		return null;
	}

	@Override
	public Void visitNode(IdNode n) {
		if (print) printNode(n);
		STentry entry = stLookup(n.id);
		if (entry == null) {
			System.out.println("Var or Par id " + n.id + " at line "+ n.getLine() + " not declared");
			stErrors++;
		} else {
			n.entry = entry;
			n.nl = nestingLevel;
		}
		return null;
	}

	@Override
	public Void visitNode(BoolNode n) {
		if (print) printNode(n, n.val.toString());
		return null;
	}

	@Override
	public Void visitNode(IntNode n) {
		if (print) printNode(n, n.val.toString());
		return null;
	}

	@Override
	public Void visitNode(ClassNode n) {
		if (print) {
			printNode(n);
		}
		//se non eredita rimane vuoto
		var classType = new ClassTypeNode(new ArrayList<>(), new ArrayList<>());

		//controllo se la classe eredita da un'altra e se tale super classe esiste
		if (n.superID != null && classTable.containsKey(n.superID)) {
			//entro qua quando eredito da un'altra classe
			//prendo la entry, il riferimento alla super classe
			STentry superClassEntry = symTable.get(0).get(n.superID);
			//setto la superclasse del nodo
			n.superClassEntry = superClassEntry;
			//grazie al riferimento copio tutto il contenuto del nodo
			classType = new ClassTypeNode(
					new ArrayList<>(((ClassTypeNode) superClassEntry.type).allFields),
					new ArrayList<>(((ClassTypeNode) superClassEntry.type).allMethods)
			);

		} else if (n.superID != null) {
			System.out.println("Extending class id " + n.superID + " at line " + n.getLine() + " is not declared");
		}
		//creo la STEntry
		STentry entry = new STentry(0, classType, decOffset--);
		n.type = classType;//gli diamo il tipo della superclasse nel caso ci sia, altrimenti un nuovo vuoto

		Map<String, STentry> globalScopeTable = symTable.get(0);
		//aggiungo il nodo alla symbolTable, se non riesco significa che era già stato dichiarato
		if (globalScopeTable.put(n.id, entry) != null) {
			System.out.println("Class id " + n.id + " at line " + n.getLine() + " already declared");
			stErrors++;
		}
		//livello dentro la dichiarazione della classe
		nestingLevel++;

		onClassVisitScope = new HashSet<>(); //spiegata in riga 15

		//la virtual table contiene tutte le entry STentry dentro la classe. esempio pag 21
		Map<String, STentry> virtualTable = new HashMap<>();
		//copiamo la virtual table relativa al nome della super classe. quindi quella da cui eredita
		var superClassVirtualTable = classTable.get(n.superID);
		if (n.superID != null) {
			//se esiste una super classe allora aggiungo la sua virtual table alla mia
			virtualTable.putAll(superClassVirtualTable);
		}
		//aggiungo sia alla classTable che alla symbolTable la virtual table
		classTable.put(n.id, virtualTable);
		symTable.add(virtualTable);

		//caso in cui non c'è overriding dei campi
		int fieldOffset = -1;
		if (n.superID != null) {
			//preservo l'offset che era nella super classe. Prendo l'offset dei campi
			fieldOffset = -classType.allFields.size()-1;
		}
		//una volta settato l'offset dei campi lavoro su di essi
		for (var field : n.fields) {
			if (onClassVisitScope.contains(field.id)) { //effettuo il controllo per i campi. spiegato a linea 15 il motivo
				System.out.println(
						"Field with id " + field.id + " on line " + field.getLine() + " was already declared"
				);
				stErrors++;
			}
			onClassVisitScope.add(field.id);
			//prendo in considerazione i campi
			var overriddenFieldEntry = virtualTable.get(field.id);
			STentry fieldEntry;
			//entro dentro l'if nel caso in cui trattiamo un campo field override
			if (overriddenFieldEntry != null && !(overriddenFieldEntry.type instanceof MethodTypeNode)) {
				//aggiorno la classTypeNode. In questo caso prendo in considerazione i campi field che sono stati override
				fieldEntry = new STentry(nestingLevel, field.getType(), overriddenFieldEntry.offset);
				//una volta che mi sono creato la entry per il field lo inserisco nella classType
				classType.allFields.set(-fieldEntry.offset - 1, fieldEntry.type);
			} else {
				//se sono qui non sto facendo override
				//aggiorno la classTypeNode. In questo caso prendo in cosiderazione i campi non override
				fieldEntry = new STentry(nestingLevel, field.getType(), fieldOffset--);
				classType.allFields.add(-fieldEntry.offset - 1, fieldEntry.type);
				if (overriddenFieldEntry != null) {
					System.out.println("Cannot override field id " + field.id + " with a method");
					stErrors++;
				}
			}
			//aggiungo i campi alla virtual table
			virtualTable.put(field.id, fieldEntry);
			field.offset = fieldEntry.offset; //inserito per migliorare efficienza del type checking, cosi da non ricontrollare due volte il field
		}
		int currentDecOffset = decOffset;
		// memorizza il contatore per l'offset delle dichiarazioni al nesting level precedente
		int prevNLDecOffset = decOffset;
		//caso in cui non c'è override
		decOffset = 0;
		//faccio quello che ho fatto per i campi ma per i metodi
		if (n.superID != null) {
			//caso in cui abbia una super classe prendo l'offset relativo ad essa che verrà poi utilizzato quando si fa la visit del metodo
			decOffset = classType.allMethods.size();
		}
		for (var method : n.methods) {
			if (onClassVisitScope.contains(method.id)) { //effettuo il controllo per i metodi anche. Motivo come per i campi spiegato a linea 15
				System.out.println(
						"Method with id " + method.id + " on line " + method.getLine() + " was already declared"
				);
				stErrors++;
			}
			//visito il metodo, all'interno della visita del metodo farò gli stessi controlli fatti per i fields riguardanti override
			visit(method);
			//aggiungo la virtual table del metodo, di cui ho appenna effettuato la visita, al classType
			classType.allMethods.add(
					method.offset,
					((MethodTypeNode) virtualTable.get(method.id).type).fun //la prendo perché in method node l'ho inserita
			);
		}
		decOffset = currentDecOffset; //poiché dentro la visita del metodo vado a cambiare il decoffset dopo lo vado a riprestinare
		symTable.remove(nestingLevel--); //rimuovere la hashmap corrente poiche' esco dallo scope
		decOffset = prevNLDecOffset; // rimetto il vecchio offset
		return null;
	}

	@Override
	public Void visitNode(ClassCallNode node) {
		if (print) {
			printNode(node);
		}
		STentry entry = stLookup(node.objectId);//objectid messo in astgeneration
		if (entry == null) {
			System.out.println("Object id " + node.objectId + " at line " + node.getLine() + " not declared");
			stErrors++;
		} else if (entry.type instanceof RefTypeNode) {
			node.entry = entry;
			node.nl = nestingLevel;
			//cerco nella virtual table la MethodEntry relativa al nodo
			// metodo lo troviammo nella ClassTable -> VirtualTable della classe di entry
			node.methodEntry = classTable.get(((RefTypeNode) entry.type).id).get(node.methodId);
			if (node.methodEntry == null) {
				System.out.println(
						"Object id " + node.objectId + " at line " + node.getLine() + " has no method " + node.methodId
				);
				stErrors++;
			}
		}

		//infine visito gli argomenti
		for (Node argument : node.argList) {
			visit(argument);
		}
		return null;
	}

	@Override
	public Void visitNode(MethodNode n) {
		if (print) printNode(n);
		//prendo la virtual table della classe
		Map<String, STentry> currentScopeTable = symTable.get(nestingLevel);
		//gestisco i tipi dei parametri
		List<TypeNode> parTypes = new ArrayList<>();
		for (ParNode par : n.parList) {
			parTypes.add(par.getType());
		}
		//faccio quello che ho fatto per i field in classNode
		//controllo se è un override
		var overriddenMethodEntry = currentScopeTable.get(n.id);
		//definisco un nodo di tipo metodo
		final TypeNode methodType = new MethodTypeNode(new ArrowTypeNode(parTypes, n.retType));
		STentry entry = null;
		//se è un override faccio come per i field e creo la entry
		if (overriddenMethodEntry != null && overriddenMethodEntry.type instanceof MethodTypeNode) {
			//creo una nuova entry con il riferimento al offset del metodo della super classe
			entry = new STentry(nestingLevel, methodType, overriddenMethodEntry.offset);
		} else {
			//nel caso in cui non sia overriden allore creo una nuova entry con in funNode
			entry = new STentry(nestingLevel, methodType, decOffset++);
			if (overriddenMethodEntry != null) {
				System.out.println("Cannot override method id " + n.id + " with a field");
				stErrors++;
			}
		}
		n.offset = entry.offset; //definisco il giusto offset nel caso ad esempio ci sia override
		currentScopeTable.put(n.id, entry); //inserisco la entry nella table così che ad esempio class node quando tratta i metodi possa recuperarla

		//creiamo una nuova tabella per il metodo
		//entriamo nel nesting level del metodo
		//simile a funnode
		nestingLevel++;
		Map<String, STentry> methodScopeTable = new HashMap<>();
		symTable.add(methodScopeTable);
		int prevNLDecOffset = decOffset;
		decOffset = -2;
		int parOffset = 1;
		for (ParNode par : n.parList) {
			final STentry parEntry = new STentry(nestingLevel, par.getType(), parOffset++);
			if (methodScopeTable.put(par.id, parEntry) != null) {
				System.out.println("Par id " + par.id + " at line " + n.getLine() + " already declared");
				stErrors++;
			}
		}
		for (Node dec : n.decList) {
			visit(dec);
		}
		visit(n.exp);
		//rimuovo la tabella(hashmap) corrente perché esco dallo scope
		symTable.remove(nestingLevel--);
		decOffset = prevNLDecOffset; // restores counter for offset of declarations at previous nesting level

		return null;
	}

	@Override
	public Void visitNode(NewNode n) {
		if (print) {
			printNode(n);
		}
		// controllo che ID sia in classTable
		if (!classTable.containsKey(n.id)) {
			System.out.println("Class id " + n.id + " was not declared");
			stErrors++;
		}
		//prendo la symbol table della classe
		n.classEntry = symTable.get(0).get(n.id);
		for (var arg : n.argList) {
			visit(arg);
		}
		return null;
	}

	@Override
	public Void visitNode(RefTypeNode n) {
		if (print) {
			printNode(n, n.id);
		}
		if (!classTable.containsKey(n.id)) {
			System.out.println("Class with id " + n.id + " on line " + n.getLine() + " was not declared");
			stErrors++;
		}
		return null;
	}

	@Override
	public Void visitNode(EmptyNode n) {
		if (print) {
			printNode(n);
		}
		return null;
	}

}
