package compiler;

import java.util.*;
import java.util.stream.Collectors;

import compiler.lib.*;

public class AST {
	
	public static class ProgLetInNode extends Node {
		final List<DecNode> declist;
		final Node exp;
		ProgLetInNode(List<DecNode> d, Node e) {
			declist = Collections.unmodifiableList(d); 
			exp = e;
		}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	public static class ProgNode extends Node {
		final Node exp;
		ProgNode(Node e) {exp = e;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class FunNode extends DecNode {
		final String id;
		final TypeNode retType;
		final List<ParNode> parlist;
		final List<DecNode> declist; 
		final Node exp;
		FunNode(String i, TypeNode rt, List<ParNode> pl, List<DecNode> dl, Node e) {
	    	id=i; 
	    	retType=rt; 
	    	parlist=Collections.unmodifiableList(pl); 
	    	declist=Collections.unmodifiableList(dl); 
	    	exp=e;
	    }
		
		//void setType(TypeNode t) {type = t;}
		
		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	public static class ParNode extends DecNode {
		final String id;
		ParNode(String i, TypeNode t) {id = i; type = t;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class VarNode extends DecNode {
		final String id;
		final Node exp;
		VarNode(String i, TypeNode t, Node v) {id = i; type = t; exp = v;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
		
	public static class PrintNode extends Node {
		final Node exp;
		PrintNode(Node e) {exp = e;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class IfNode extends Node {
		final Node cond;
		final Node th;
		final Node el;
		IfNode(Node c, Node t, Node e) {cond = c; th = t; el = e;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class EqualNode extends Node {
		final Node left;
		final Node right;
		EqualNode(Node l, Node r) {left = l; right = r;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	public static class GreaterEqualNode extends Node {

		final Node left;
		final Node right;

		GreaterEqualNode(Node l, Node r) {
			this.left = l;
			this.right = r;
		}

		@Override
		public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
			return visitor.visitNode(this);
		}
	}

	public static class LessEqualNode extends Node {

		final Node left;
		final Node right;

		LessEqualNode(Node l, Node r) {
			this.left = l;
			this.right = r;
		}

		@Override
		public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
			return visitor.visitNode(this);
		}
	}

	public static class OrNode extends Node {

		final Node left;
		final Node right;

		OrNode(Node l, Node r) {
			this.left = l;
			this.right = r;
		}

		@Override
		public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
			return visitor.visitNode(this);
		}
	}

	public static class AndNode extends Node {

		final Node left;
		final Node right;

		AndNode(Node l, Node r) {
			this.left = l;
			this.right = r;
		}

		@Override
		public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
			return visitor.visitNode(this);
		}
	}

	public static class NotNode extends Node {

		final Node exp;

		NotNode(Node exp) {
			this.exp = exp;
		}

		@Override
		public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
			return visitor.visitNode(this);
		}
	}

	public static class TimesNode extends Node {
		final Node left;
		final Node right;
		TimesNode(Node l, Node r) {left = l; right = r;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	public static class DivNode extends Node {

		final Node left;
		final Node right;

		DivNode(Node l, Node r) {
			this.left = l;
			this.right = r;
		}

		@Override
		public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
			return visitor.visitNode(this);
		}
	}

	public static class PlusNode extends Node {
		final Node left;
		final Node right;
		PlusNode(Node l, Node r) {left = l; right = r;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	public static class MinusNode extends Node {

		final Node left;
		final Node right;

		MinusNode(Node l, Node r) {
			this.left = l;
			this.right = r;
		}

		@Override
		public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
			return visitor.visitNode(this);
		}
	}

	public static class CallNode extends Node {
		final String id;
		final List<Node> arglist;
		STentry entry;
		int nl; //utilizzato per sapere il nesting level di dove siamo e poi prendere gli elementi all'interno ricontrollando il nesting level di nuovo
		CallNode(String i, List<Node> p) {//p è arglist
			id = i; 
			arglist = Collections.unmodifiableList(p);
		}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	public static class ClassCallNode extends Node {

		final String objectId;
		final String methodId;

		int nl; //nesting level
		STentry entry;

		STentry methodEntry;
		final List<Node> argList;

		public ClassCallNode(String objectId, String methodId, List<Node> argList) {
			this.objectId = objectId;
			this.methodId = methodId;
			this.argList = argList; //arguments list
		}

		@Override
		public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
			return visitor.visitNode(this);
		}
	}
	
	public static class IdNode extends Node {
		final String id;
		STentry entry;
		int nl;
		IdNode(String i) {id = i;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class BoolNode extends Node {
		final Boolean val;
		BoolNode(boolean n) {val = n;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class IntNode extends Node {
		final Integer val;
		IntNode(Integer n) {val = n;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	//serve per il ritorno delle funzioni
	public static class ArrowTypeNode extends TypeNode {
		final List<TypeNode> parlist;
		final TypeNode ret;
		ArrowTypeNode(List<TypeNode> p, TypeNode r) {
			parlist = Collections.unmodifiableList(p); 
			ret = r;
		}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class BoolTypeNode extends TypeNode {

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	public static class IntTypeNode extends TypeNode {

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	public static class ClassNode extends DecNode {

		final String id;
		final String superID;
		final List<FieldNode> fields;
		final List<MethodNode> methods;
		STentry superClassEntry;
		ClassTypeNode type;

		public ClassNode(String id, String superID, List<FieldNode> fields, List<MethodNode> methods) {
			this.id = id;
			this.superID = superID;
			this.fields = Collections.unmodifiableList(fields);
			this.methods = Collections.unmodifiableList(methods);
		}

		@Override
		public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
			return visitor.visitNode(this);
		}
	}

	public static class ClassTypeNode extends TypeNode {

		final List<TypeNode> allFields; //tipi dei campi, anche quelli ereditati
		final List<ArrowTypeNode> allMethods;//tipi funzionali metodi, inclusi quelli ereditati

		ClassTypeNode(List<TypeNode> allFields, List<ArrowTypeNode> allMethods) {
			this.allFields = allFields;
			this.allMethods = allMethods;
		}

		@Override
		public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
			return visitor.visitNode(this);
		}
	}

	public static class FieldNode extends DecNode {

		final String id;
		int offset; //inserito per rendere più efficiente il type checking

		FieldNode(String id, TypeNode type) {
			this.id = id;
			this.type = type;
		}

		@Override
		public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
			return visitor.visitNode(this);
		}
	}

	public static class MethodNode extends DecNode {

		final String id;
		final TypeNode retType;
		final List<ParNode> parList;
		final List<DecNode> decList;
		final Node exp;
		int offset;
		String label; //code generation

		MethodNode(String id, TypeNode retType, List<ParNode> parList, List<DecNode> decList, Node exp) {
			this.id = id;
			this.retType = retType;
			this.parList = parList;
			this.decList = decList;
			this.exp = exp;
			this.type = new ArrowTypeNode(this.parList.stream().map(ParNode::getType).collect(Collectors.toList()), this.retType);
		}

		@Override
		public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
			return visitor.visitNode(this);
		}
	}

	public static class MethodTypeNode extends TypeNode {

		final ArrowTypeNode fun;//è function type

		public MethodTypeNode( ArrowTypeNode fun) {
			this.fun = fun;
		}

		@Override
		public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
			return visitor.visitNode(this);
		}
	}

	public static class NewNode extends Node {

		STentry classEntry;
		final String id; //id della classe
		List<Node> argList;

		NewNode(String id, List<Node> argList) {
			this.id = id;
			this.argList = Collections.unmodifiableList(argList);
		}

		@Override
		public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
			return visitor.visitNode(this);
		}
	}
	//si usa quando abbiamo null
	public static class EmptyNode extends Node {

		@Override
		public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
			return visitor.visitNode(this);
		}
	}
	//lo usiamo quando facciamo typecheck per dire che è un emptyNode
	public static class EmptyTypeNode extends TypeNode {

		@Override
		public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
			return visitor.visitNode(this);
		}
	}
	//contiene l'ID della classe come campo
	public static class RefTypeNode extends TypeNode {

		final String id;
		RefTypeNode(String id) {
			this.id = id;
		}

		@Override
		public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
			return visitor.visitNode(this);
		}
	}


}