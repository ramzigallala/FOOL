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
	Set<String> onClassVisitScope;

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
	public Void visitNode(LessEqualNode node) {
		if (print) {
			printNode(node);
		}
		visit(node.left);
		visit(node.right);
		return null;
	}

	@Override
	public Void visitNode(GreaterEqualNode node) {
		if (print) {
			printNode(node);
		}
		visit(node.left);
		visit(node.right);
		return null;
	}

	@Override
	public Void visitNode(AndNode node) {
		if (print) {
			printNode(node);
		}
		visit(node.left);
		visit(node.right);
		return null;
	}

	@Override
	public Void visitNode(OrNode node) {
		if (print) {
			printNode(node);
		}
		visit(node.left);
		visit(node.right);
		return null;
	}

	@Override
	public Void visitNode(NotNode node) {
		if (print) {
			printNode(node);
		}
		visit(node.expression);
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
	public Void visitNode(DivNode node) {
		if (print) {
			printNode(node);
		}
		visit(node.left);
		visit(node.right);
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
	public Void visitNode(MinusNode node) {
		if (print) {
			printNode(node);
		}
		visit(node.left);
		visit(node.right);
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
	public Void visitNode(ClassNode node) {
		if (print) {
			printNode(node);
		}
		var classType = new ClassTypeNode(new ArrayList<>(), new ArrayList<>());
		/*
		 * Class is extending
		 */
		if (node.superID != null && classTable.containsKey(node.superID)) {
			//entro qua quando eredito da un'altra classe, su cui non ho fatto overriding
			//prendo la entry, il riferimento alla super classe
			STentry superClassEntry = symTable.get(0).get(node.superID);
			//grazie al riferimento copio tutto il contenuto del nodo
			classType = new ClassTypeNode(
					new ArrayList<>(((ClassTypeNode) superClassEntry.type).allFields),
					new ArrayList<>(((ClassTypeNode) superClassEntry.type).allMethods)
			);
			//setto la superclasse del nodo
			node.superClassEntry = superClassEntry;
		} else if (node.superID != null) {
			System.out.println("Extending class id " + node.superID + " at line " + node.getLine() + " is not declared");
		}
		STentry entry = new STentry(0, classType, decOffset--);
		node.type = classType;//gli diamo il tipo della superclasse nel caso ci sia
		Map<String, STentry> globalScopeTable = symTable.get(0);
		//aggiungo il nodo alla symbolTable
		if (globalScopeTable.put(node.id, entry) != null) {
			System.out.println("Class id " + node.id + " at line " + node.getLine() + " already declared");
			stErrors++;
		}
		/*
		 * Add a the scope table for the id of the class.
		 * Table should be added for both symbol table and class table.
		 */
		nestingLevel++;
		onClassVisitScope = new HashSet<>();
		//la virtual table contiene tutte le entry STentry dentro la classe. esempio pag 21
		Map<String, STentry> virtualTable = new HashMap<>();
		//copiamo la virtual table relativa al nome della super classe. quindi quella da cui eredita
		var superClassVirtualTable = classTable.get(node.superID);
		if (node.superID != null) {
			//se esiste una super classe allora aggiungo la sua virtual table alla mia
			virtualTable.putAll(superClassVirtualTable);
		}
		//aggiungo sia alla classTable che alla sybleTable la virtual table
		classTable.put(node.id, virtualTable);
		symTable.add(virtualTable);
		/*
		 * Setting the fieldOffset for the extending class
		 */
		int fieldOffset = -1;
		if (node.superID != null) {
			//preservo l'offset che era nella vecchia STentry
			fieldOffset = -((ClassTypeNode) symTable.get(0).get(node.superID).type).allFields.size()-1;
		}
		/*
		 * Handle field declaration.
		 */

		for (var field : node.fields) {
			if (onClassVisitScope.contains(field.id)) {
				System.out.println(
						"Field with id " + field.id + " on line " + field.getLine() + " was already declared"
				);
				stErrors++;
			}
			onClassVisitScope.add(field.id);
			//prendo in considerazione i campi
			var overriddenFieldEntry = virtualTable.get(field.id);
			STentry fieldEntry;
			if (overriddenFieldEntry != null && !(overriddenFieldEntry.type instanceof MethodTypeNode)) {
				//aggiorno la classTypeNode. In questo caso prendo in considerazione i campi field che sono stati override
				fieldEntry = new STentry(nestingLevel, field.getType(), overriddenFieldEntry.offset);
				//una volta che mi sono creato la entry per il field lo inserisco nella classType
				classType.allFields.set(-fieldEntry.offset - 1, fieldEntry.type);
			} else {
				//aggiorno la classTypeNode. In questo caso prendo in cosiderazione i campi non override
				fieldEntry = new STentry(nestingLevel, field.getType(), fieldOffset--);
				classType.allFields.add(-fieldEntry.offset - 1, fieldEntry.type);
				if (overriddenFieldEntry != null) {
					System.out.println("Cannot override field id " + field.id + " with a method");
					stErrors++;
				}
			}
			/*
			 * Add field id in symbol(virtual) table
			 */
			virtualTable.put(field.id, fieldEntry);
			field.offset = fieldEntry.offset;
		}
		int currentDecOffset = decOffset;
		// method declarationOffset starts from 0
		int previousNestingLevelDeclarationOffset = decOffset;
		decOffset = 0;
		//faccio quello che ho fatto per i campi ma per i metodi
		if (node.superID != null) {
			//caso in cui abbia una super classe prendo l'ofset relativo ad essa che verrà poi utilizzato quando si faà l visit del metodo
			decOffset = ((ClassTypeNode) symTable.get(0).get(node.superID).type).allMethods.size();
		}
		for (var method : node.methods) {
			if (onClassVisitScope.contains(method.id)) {
				System.out.println(
						"Method with id " + method.id + " on line " + method.getLine() + " was already declared"
				);
				stErrors++;
			}
			//visito il metodo
			visit(method);
			//aggiungo la virtual table del metodo, di cui ho appenna effettuato la visita, al classType
			classType.allMethods.add(
					method.offset,
					((MethodTypeNode) virtualTable.get(method.id).type).functionalType
			);
		}
		decOffset = currentDecOffset; // restores the previous declaration offset
		symTable.remove(nestingLevel--);
		decOffset = previousNestingLevelDeclarationOffset;
		return null;
	}

	@Override
	public Void visitNode(ClassCallNode node) {
		if (print) {
			printNode(node);
		}
		STentry entry = stLookup(node.objectId);
		if (entry == null) {
			System.out.println("Object id " + node.objectId + " at line " + node.getLine() + " not declared");
			stErrors++;
		} else if (entry.type instanceof RefTypeNode) {
			node.symbolTableEntry = entry;
			node.nestingLevel = nestingLevel;
			//cerco nella virtual table la MethodEntry relativa la nodo
			node.methodEntry = classTable.get(((RefTypeNode) entry.type).id).get(node.methodId);
			if (node.methodEntry == null) {
				System.out.println(
						"Object id " + node.objectId + " at line " + node.getLine() + " has no method " + node.methodId
				);
				stErrors++;
			}
		}
		for (Node argument : node.argumentsList) {
			visit(argument);
		}
		return null;
	}

	@Override
	public Void visitNode(MethodNode node) {
		if (print) printNode(node);
		Map<String, STentry> currentScopeTable = symTable.get(nestingLevel);
		List<TypeNode> parametersTypes = new ArrayList<>();
		for (ParNode parameter : node.parametersList) {
			parametersTypes.add(parameter.getType());
		}
		/*
		 * Insert ID into the symbolTable.
		 */
		//come per FunNode solo che in questo caso bisogna controllare i metodi overriden
		var overriddenMethodEntry = currentScopeTable.get(node.id);
		final TypeNode methodType = new MethodTypeNode(new ArrowTypeNode(parametersTypes, node.returnType));
		STentry entry = null;
		if (overriddenMethodEntry != null && overriddenMethodEntry.type instanceof MethodTypeNode) {
			//creo una nuova entry con il riferimento al offset del metodo della super classe
			entry = new STentry(nestingLevel, methodType, overriddenMethodEntry.offset);
		} else {
			//nel caso in cui non sia overriden allore creo una nuova entry con in funNode
			entry = new STentry(nestingLevel, methodType, decOffset++);
			if (overriddenMethodEntry != null) {
				System.out.println("Cannot override method id " + node.id + " with a field");
				stErrors++;
			}
		}
		node.offset = entry.offset;
		currentScopeTable.put(node.id, entry);
		/*
		 * Create a new table for the method.
		 */
		nestingLevel++;
		Map<String, STentry> methodScopeTable = new HashMap<>();
		symTable.add(methodScopeTable);
		int previousNestingLeveleDeclarationOffset = decOffset;
		decOffset = -2;
		int parameterOffset = 1;
		for (ParNode parameter : node.parametersList) {
			final STentry parameterEntry = new STentry(nestingLevel, parameter.getType(), parameterOffset++);
			if (methodScopeTable.put(parameter.id, parameterEntry) != null) {
				System.out.println("Par id " + parameter.id + " at line " + node.getLine() + " already declared");
				stErrors++;
			}
		}
		for (Node declaration : node.declarationsList) {
			visit(declaration);
		}
		visit(node.expression);
		/*
		 * Remove the current nesting level symbolTable.
		 */
		symTable.remove(nestingLevel--);
		decOffset = previousNestingLeveleDeclarationOffset;
		return null;
	}

	@Override
	public Void visitNode(NewNode node) {
		if (print) {
			printNode(node);
		}
		if (!classTable.containsKey(node.id)) {
			System.out.println("Class id " + node.id + " was not declared");
			stErrors++;
		}
		node.classSymbolTableEntry = symTable.get(0).get(node.id);
		for (var argument : node.argumentsList) {
			visit(argument);
		}
		return null;
	}

	@Override
	public Void visitNode(RefTypeNode node) {
		if (print) {
			printNode(node, node.id);
		}
		if (!classTable.containsKey(node.id)) {
			System.out.println("Class with id " + node.id + " on line " + node.getLine() + " was not declared");
			stErrors++;
		}
		return null;
	}

	@Override
	public Void visitNode(EmptyNode node) {
		if (print) {
			printNode(node);
		}
		return null;
	}

}
