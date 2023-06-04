package compiler;

import compiler.AST.*;
import compiler.lib.*;
import compiler.exc.*;
//albero di natale ovvero il AST unito con le STentry
public class PrintEASTVisitor extends BaseEASTVisitor<Void,VoidException> {

	PrintEASTVisitor() { super(false,true); } 

	@Override
	public Void visitNode(ProgLetInNode n) {
		printNode(n);
		for (Node dec : n.declist) visit(dec);
		visit(n.exp);
		return null;
	}

	@Override
	public Void visitNode(ProgNode n) {
		printNode(n);
		visit(n.exp);
		return null;
	}

	@Override
	public Void visitNode(ClassNode node) {
		printNode(node, node.id);
		for (var field : node.fields) {
			visit(field);
		}
		for (var method : node.methods) {
			visit(method);
		}
		return null;
	}

	@Override
	public Void visitNode(FieldNode node) {
		printNode(node, node.id);
		visit(node.getType());
		return null;
	}

	@Override
	public Void visitNode(MethodNode node) {
		printNode(node, node.id);
		visit(node.retType);
		for (ParNode parameter : node.parList) {
			visit(parameter);
		}
		for (DecNode declaration : node.decList) {
			visit(declaration);
		}
		visit(node.exp);
		return null;
	}

	@Override
	public Void visitNode(NewNode node) {
		printNode(node, node.id);
		visit(node.classEntry);
		return null;
	}

	@Override
	public Void visitNode(RefTypeNode node) {
		printNode(node, node.id);
		return null;
	}

	@Override
	public Void visitNode(FunNode n) {
		printNode(n,n.id);
		visit(n.retType);
		for (ParNode par : n.parlist) visit(par);
		for (Node dec : n.declist) visit(dec);
		visit(n.exp);
		return null;
	}

	@Override
	public Void visitNode(ParNode n) {
		printNode(n,n.id);
		visit(n.getType());
		return null;
	}

	@Override
	public Void visitNode(VarNode n) {
		printNode(n,n.id);
		visit(n.getType());
		visit(n.exp);
		return null;
	}

	@Override
	public Void visitNode(PrintNode n) {
		printNode(n);
		visit(n.exp);
		return null;
	}

	@Override
	public Void visitNode(IfNode n) {
		printNode(n);
		visit(n.cond);
		visit(n.th);
		visit(n.el);
		return null;
	}

	@Override
	public Void visitNode(EqualNode n) {
		printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(LessEqualNode node) {
		printNode(node);
		visit(node.left);
		visit(node.right);
		return null;
	}

	@Override
	public Void visitNode(GreaterEqualNode node) {
		printNode(node);
		visit(node.left);
		visit(node.right);
		return null;
	}

	@Override
	public Void visitNode(OrNode node) {
		printNode(node);
		visit(node.left);
		visit(node.right);
		return null;
	}

	@Override
	public Void visitNode(AndNode node) {
		printNode(node);
		visit(node.left);
		visit(node.right);
		return null;
	}

	@Override
	public Void visitNode(TimesNode n) {
		printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(DivNode node) {
		printNode(node);
		visit(node.left);
		visit(node.right);
		return null;
	}

	@Override
	public Void visitNode(PlusNode n) {
		printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(MinusNode node) {
		printNode(node);
		visit(node.left);
		visit(node.right);
		return null;
	}

	@Override
	public Void visitNode(CallNode n) {
		printNode(n,n.id+" at nestinglevel "+n.nl); 
		visit(n.entry);
		for (Node arg : n.arglist) visit(arg);
		return null;
	}

	@Override
	public Void visitNode(ClassCallNode node) {
		printNode(node, node.objectId + "." +  node.methodId + " at nestingLevel " + node.nl);
		visit(node.methodEntry);
		visit(node.methodEntry);
		for (Node argument : node.argList) {
			visit(argument);
		}
		return null;
	}

	@Override
	public Void visitNode(IdNode n) {
		printNode(n,n.id+" at nestinglevel "+n.nl); 
		visit(n.entry);
		return null;
	}

	@Override
	public Void visitNode(BoolNode n) {
		printNode(n,n.val.toString());
		return null;
	}

	@Override
	public Void visitNode(IntNode n) {
		printNode(n,n.val.toString());
		return null;
	}
	
	@Override
	public Void visitNode(ArrowTypeNode n) {
		printNode(n);
		for (Node par: n.parlist) visit(par);
		visit(n.ret,"->"); //marks return type
		return null;
	}

	@Override
	public Void visitNode(BoolTypeNode n) {
		printNode(n);
		return null;
	}

	@Override
	public Void visitNode(IntTypeNode n) {
		printNode(n);
		return null;
	}

	@Override
	public Void visitNode(ClassTypeNode node) {
		printNode(node);
		for(var field : node.allFields) {
			visit(field);
		}
		for (var method : node.allMethods) {
			visit(method);
		}
		return null;
	}

	@Override
	public Void visitNode(MethodTypeNode node) {
		visit(node.fun);
		return null;
	}

	@Override
	public Void visitNode(EmptyNode node) {
		printNode(node);
		return null;
	}

	@Override
	public Void visitNode(NotNode node) {
		printNode(node);
		visit(node.exp);
		return null;
	}

	@Override
	public Void visitSTentry(STentry entry) {
		printSTentry("nestlev "+entry.nl); //stampa il nesting level
		printSTentry("type");
		visit(entry.type);
		printSTentry("offset "+entry.offset);
		return null;
	}

}
