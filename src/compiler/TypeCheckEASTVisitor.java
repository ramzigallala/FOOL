package compiler;

import compiler.AST.*;
import compiler.exc.*;
import compiler.lib.*;
import static compiler.TypeRels.*;

//visitNode(n) fa il type checking di un Node n e ritorna:
//- per una espressione, il suo tipo (oggetto BoolTypeNode o IntTypeNode)
//- per una dichiarazione, "null"; controlla la correttezza interna della dichiarazione
//(- per un tipo: "null"; controlla che il tipo non sia incompleto) 
//
//visitSTentry(s) ritorna, per una STentry s, il tipo contenuto al suo interno
public class TypeCheckEASTVisitor extends BaseEASTVisitor<TypeNode,TypeException> {

	TypeCheckEASTVisitor() { super(true); } // enables incomplete tree exceptions 
	TypeCheckEASTVisitor(boolean debug) { super(true,debug); } // enables print for debugging

	//checks that a type object is visitable (not incomplete) 
	private TypeNode ckvisit(TypeNode t) throws TypeException {
		visit(t);
		return t;
	} 
	
	@Override
	public TypeNode visitNode(ProgLetInNode n) throws TypeException {
		if (print) printNode(n);
		for (Node dec : n.declist)
			try {
				visit(dec);
			} catch (IncomplException e) { 
			} catch (TypeException e) {
				System.out.println("Type checking error in a declaration: " + e.text);
			}
		return visit(n.exp);
	}

	@Override
	public TypeNode visitNode(ProgNode n) throws TypeException {
		if (print) printNode(n);
		return visit(n.exp);
	}

	@Override
	public TypeNode visitNode(FunNode n) throws TypeException {
		if (print) printNode(n,n.id);
		for (Node dec : n.declist)
			try {
				visit(dec);
			} catch (IncomplException e) { 
			} catch (TypeException e) {
				System.out.println("Type checking error in a declaration: " + e.text);
			}
		if ( !isSubtype(visit(n.exp),ckvisit(n.retType)) ) 
			throw new TypeException("Wrong return type for function " + n.id,n.getLine());
		return null;
	}

	@Override
	public TypeNode visitNode(VarNode n) throws TypeException {
		if (print) printNode(n,n.id);
		if ( !isSubtype(visit(n.exp),ckvisit(n.getType())) )
			throw new TypeException("Incompatible value for variable " + n.id,n.getLine());
		return null;
	}

	@Override
	public TypeNode visitNode(PrintNode n) throws TypeException {
		if (print) printNode(n);
		return visit(n.exp);
	}

	@Override
	public TypeNode visitNode(IfNode n) throws TypeException {
		if (print) printNode(n);
		if ( !(isSubtype(visit(n.cond), new BoolTypeNode())) )
			throw new TypeException("Non boolean condition in if",n.getLine());
		TypeNode t = visit(n.th);
		TypeNode e = visit(n.el);
		TypeNode lowestCommonAncestor = lowestCommonAncestor(t,e);
		//ritorniamo il sopratipo cosi siamo sicuri che vada bene. es tra car e ford torno car perché cosi siamo sicuri che dentro ci va ford ma anche fiat ect.
		if(lowestCommonAncestor == null)
			throw new TypeException("Incompatible types in then-else branches",n.getLine());
		return lowestCommonAncestor;
	}

	@Override
	public TypeNode visitNode(EqualNode n) throws TypeException {
		if (print) printNode(n);
		TypeNode l = visit(n.left);
		TypeNode r = visit(n.right);
		if ( !(isSubtype(l, r) || isSubtype(r, l)) )
			throw new TypeException("Incompatible types in equal",n.getLine());
		return new BoolTypeNode();
	}

	@Override
	public TypeNode visitNode(LessEqualNode n) throws TypeException {
		if (print) {
			printNode(n);
		}
		TypeNode left = visit(n.left);
		TypeNode right = visit(n.right);
		if (!(isSubtype(left, right) || isSubtype(right, left))) {
			throw new TypeException("Incompatible types in less equal",n.getLine());
		}
		return new BoolTypeNode();
	}

	@Override
	public TypeNode visitNode(GreaterEqualNode n) throws TypeException {
		if (print) {
			printNode(n);
		}
		TypeNode left = visit(n.left);
		TypeNode right = visit(n.right);
		if (!(isSubtype(left, right) || isSubtype(right, left))) {
			throw new TypeException("Incompatible types in greater equal",n.getLine());
		}
		return new BoolTypeNode();
	}

	@Override
	public TypeNode visitNode(OrNode n) throws TypeException {
		if (print) {
			printNode(n);
		}
		TypeNode left = visit(n.left);
		TypeNode right = visit(n.right);
		if (!(isSubtype(left, right) || isSubtype(right, left))) {
			throw new TypeException("Incompatible types in OR ",n.getLine());
		}
		return new BoolTypeNode();
	}

	@Override
	public TypeNode visitNode(AndNode n) throws TypeException {
		if (print) {
			printNode(n);
		}
		TypeNode left = visit(n.left);
		TypeNode right = visit(n.right);
		if (!(isSubtype(left, right) || isSubtype(right, left))) {
			throw new TypeException("Incompatible types in AND",n.getLine());
		}
		return new BoolTypeNode();
	}

	@Override
	public TypeNode visitNode(DivNode n) throws TypeException {
		if (print) {
			printNode(n);
		}
		if (!(isSubtype(visit(n.left), new IntTypeNode())
				&& isSubtype(visit(n.right), new IntTypeNode()))) {
			throw new TypeException("Non integers in division",n.getLine());
		}
		return new IntTypeNode();
	}

	@Override
	public TypeNode visitNode(MinusNode n) throws TypeException {
		if (print) {
			printNode(n);
		}
		if ( !(isSubtype(visit(n.left), new IntTypeNode())
				&& isSubtype(visit(n.right), new IntTypeNode())) ) {
			throw new TypeException("Non integers in sub",n.getLine());
		}
		return new IntTypeNode();
	}

	@Override
	public TypeNode visitNode(NotNode n) throws TypeException {
		if (print) {
			printNode(n);

		}
		TypeNode expressionType = visit(n.exp);
		if (!(isSubtype(expressionType, new IntTypeNode()))) {
			throw new TypeException("Incompatible type in not", n.getLine());
		}
		return new BoolTypeNode();
	}

	@Override
	public TypeNode visitNode(TimesNode n) throws TypeException {
		if (print) printNode(n);
		if ( !(isSubtype(visit(n.left), new IntTypeNode())
				&& isSubtype(visit(n.right), new IntTypeNode())) )
			throw new TypeException("Non integers in multiplication",n.getLine());
		return new IntTypeNode();
	}

	@Override
	public TypeNode visitNode(PlusNode n) throws TypeException {
		if (print) printNode(n);
		if ( !(isSubtype(visit(n.left), new IntTypeNode())
				&& isSubtype(visit(n.right), new IntTypeNode())) )
			throw new TypeException("Non integers in sum",n.getLine());
		return new IntTypeNode();
	}
	//ritorno il tipo dichiarato per la funzione
	@Override
	public TypeNode visitNode(CallNode n) throws TypeException {
		if (print) printNode(n,n.id);
		TypeNode t = visit(n.entry);
		//nel caso in cui sia methodTypeNode allore prendo il tipo della funzione
		if (t instanceof MethodTypeNode) {
			t = ((MethodTypeNode) t).fun;
		} else if ( !(t instanceof ArrowTypeNode) )
			throw new TypeException("Invocation of a non-function "+n.id,n.getLine());

		ArrowTypeNode at = (ArrowTypeNode) t;
		//da qui in poi uguale
		if ( !(at.parlist.size() == n.arglist.size()) )
			throw new TypeException("Wrong number of parameters in the invocation of "+n.id,n.getLine());

		for (int i = 0; i < n.arglist.size(); i++)
			if ( !(isSubtype(visit(n.arglist.get(i)),at.parlist.get(i))) )
				throw new TypeException("Wrong type for "+(i+1)+"-th parameter in the invocation of "+n.id,n.getLine());
		return at.ret;
	}

	@Override
	public TypeNode visitNode(NewNode n) throws TypeException {
		if (print) {
			printNode(n, n.id);
		}
		//prendo la entry relativa alla classe salvata nella symbolTable. QUindi quello che fa il metodo è: prendo allFileds della classe, definita prima, e li confornto con i parametri definiti nella sua istanziazione
		var classFields = ((ClassTypeNode) n.classEntry.type).allFields;
		//controllo che nella new non abbiamo definito il numero sbagliato di parametri
		if (n.argList.size() != classFields.size()) {
			throw new TypeException(
					"Wrong number of parameters for new instance of class id " + n.id, n.getLine()
			);
		}
		//controllo che i parametri definiti nella new siano un sottotipo dei parametri definiti nella definizione della classe
		for (var i = 0; i < classFields.size(); i++) {
			if (!isSubtype(visit(n.argList.get(i)), classFields.get(i))) {
				throw new TypeException(
						"Wrong type for " + (i+1) + "-th parameter in the invocation of " + n.id,
						n.getLine()
				);
			}
		}
		//torno il riferimento alla classe
		return new RefTypeNode(n.id);
	}

	@Override
	public TypeNode visitNode(EmptyNode n) {
		if (print) {
			printNode(n);
		}
		return new EmptyTypeNode();
	}

	@Override
	public TypeNode visitNode(ClassCallNode node) throws TypeException {
		if (print) {
			printNode(node, node.objectId+"."+node.methodId);
		}
		//come callNode
		TypeNode methodType = visit(node.methodEntry);
		if (!(methodType instanceof MethodTypeNode)) {
			throw new TypeException("Invocation of a non-method " + node.methodId, node.getLine());
		}
		ArrowTypeNode at = ((MethodTypeNode) methodType).fun;
		if (!(at.parlist.size() == node.argList.size())) {
			throw new TypeException("Wrong number of parameters in the invocation of " + node.methodId, node.getLine());
		}
		for (var i = 0; i < node.argList.size(); i++) {
			if (!isSubtype(visit(node.argList.get(i)), at.parlist.get(i))) {
				throw new TypeException(
						"Wrong type for " + (i+1) + "-th parameter in the invocation of " + node.methodId, node.getLine()
				);
			}
		}
		return at.ret;
	}

	@Override
	public TypeNode visitNode(IdNode n) throws TypeException {
		if (print) printNode(n,n.id);
		TypeNode t = visit(n.entry);
		if (t instanceof ArrowTypeNode) {
			throw new TypeException("Wrong usage of function identifier " + n.id,n.getLine());
		}else if (t instanceof MethodTypeNode) {
			throw new TypeException("Wrong usage of method identifier " + n.id,n.getLine());
		} else if (t instanceof  ClassTypeNode) {
			throw new TypeException("Wrong usage of class identifier " + n.id,n.getLine());
		}
		return t;
	}

	@Override
	public TypeNode visitNode(BoolNode n) {
		if (print) printNode(n,n.val.toString());
		return new BoolTypeNode();
	}

	@Override
	public TypeNode visitNode(IntNode n) {
		if (print) printNode(n,n.val.toString());
		return new IntTypeNode();
	}

// gestione tipi incompleti	(se lo sono lancia eccezione)
	
	@Override
	public TypeNode visitNode(ArrowTypeNode n) throws TypeException {
		if (print) printNode(n);
		for (Node par: n.parlist) visit(par);
		visit(n.ret,"->"); //marks return type
		return null;
	}

	@Override
	public TypeNode visitNode(BoolTypeNode n) {
		if (print) printNode(n);
		return null;
	}

	@Override
	public TypeNode visitNode(IntTypeNode n) {
		if (print) printNode(n);
		return null;
	}

	@Override
	public TypeNode visitNode(ClassNode n) throws TypeException {
		if (print) {
			printNode(n, n.id);
		}

		//eredito
		if (n.superID != null) {
			superType.put(n.id, n.superID); //aggiorno la mappa
			ClassTypeNode classType = n.type;
			ClassTypeNode parentClassType = (ClassTypeNode) n.superClassEntry.type; //utilizzato per rendere più efficiente il type checking
			for (var field : n.fields) {
				int position = -field.offset-1;
				//controllo che eventuali overriding siano corretti nei fields.
				//aggiunto nelle ottimizzazioni
				if (position < parentClassType.allFields.size() //controllo che stiamo lavorando nei fields della super classe. Quindi per essere sicuro che faccio override
						&& !isSubtype(classType.allFields.get(position), parentClassType.allFields.get(position))) {//controllo che l'overriding dei tipi dei field nella classe figlio siano sottotipo dei field nella classe padre
					throw new TypeException("Wrong type for field " + field.id, field.getLine());
				}
			}
			//stessa cosa che ho fatto per i field la replico per i nodi metodo
			for (var method : n.methods) {
				int position = method.offset;
				if (position < parentClassType.allMethods.size()
						&& !isSubtype(classType.allMethods.get(position), parentClassType.allMethods.get(position))) {
					throw new TypeException("Wrong type for method " + method.id, method.getLine());
				}
			}
		}else{
			//non eredito
			//visito i metodi della classe per vedere se vanno bene
			for (var method : n.methods) {
				visit(method);
			}
		}
		return null;
	}

	@Override
	public TypeNode visitNode(MethodNode n) throws TypeException {
		if (print) {
			printNode(n, n.id);
		}
		for (Node dec : n.decList) {
			try {
				//visito le dichiarazioni dei metodi per vedere se vanno bene
				visit(dec);
			} catch (IncomplException e) {
			} catch (TypeException e) {
				System.out.println("Type checking error in a declaration: " + e.text);
			}
		}
		//controllo se il tipo del risultato della espressione e un sotto tipo del ritorno
		if (!isSubtype(visit(n.exp), ckvisit(n.retType))) {
			throw new TypeException("Wrong return type for method " + n.id,n.getLine());
		}
		return null;
	}

	@Override
	public TypeNode visitNode(MethodTypeNode n) throws TypeException {
		if (print) {
			printNode(n);
		}
		visit(n.fun);
		return null;
	}

	@Override
	public TypeNode visitNode(RefTypeNode node) {
		if (print) {
			printNode(node);
		}
		return null;
	}

// STentry (ritorna campo type)

	@Override
	public TypeNode visitSTentry(STentry entry) throws TypeException {
		if (print) printSTentry("type");
		return ckvisit(entry.type); 
	}

}