package compiler;
//genera l'AST visitando il parse tree
//visitando l'abero posso ad esempio calcolare le espressioni 5+5+9
//la classe che estendiamo ci viene data da ANTLR4
//il nodo radice dell'albero è prog
//oltre a prog per ogni # avrò un visit. il nome del metodo è visit seguito dal nome della produzione. es #ciao avrò vistciao() come metodo
//dal parse tree generiamo l'AST. visitiamo il parse tree attraverso il patter visitor
import java.util.*;
import java.util.stream.IntStream;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import compiler.AST.*;
import compiler.FOOLParser.*;
import compiler.lib.*;
import static compiler.lib.FOOLlib.*;

public class ASTGenerationSTVisitor extends FOOLBaseVisitor<Node> {

	String indent;
    public boolean print;
	
    ASTGenerationSTVisitor() {}    
    ASTGenerationSTVisitor(boolean debug) { print=debug; }
        
    private void printVarAndProdName(ParserRuleContext ctx) {
        String prefix="";        
    	Class<?> ctxClass=ctx.getClass(), parentClass=ctxClass.getSuperclass();
        if (!parentClass.equals(ParserRuleContext.class)) // parentClass is the var context (and not ctxClass itself)
        	prefix=lowerizeFirstChar(extractCtxName(parentClass.getName()))+": production #";
    	System.out.println(indent+prefix+lowerizeFirstChar(extractCtxName(ctxClass.getName())));                               	
    }
	//ne abbiamo fatto l'override solo perché vogliamo iddentare il risultato cosi da vedere l'albero sintattico
    @Override
	public Node visit(ParseTree t) {
		//serve per non bloccare tutto nel caso abbiamo un ST incompleto. Ovviamente avremo un AST incompleto
    	if (t==null) return null;
        String temp=indent;
        indent=(indent==null)?"":indent+"  ";
        Node result = super.visit(t); //per far tutto andare normale una volta calcolato l'indent
        indent=temp;
        return result; 
	}
	//prog non ha nulla quindi gli dico di vedere il figlio(nomefiglio: progbody)
	@Override
	public Node visitProg(ProgContext c) {
		if (print) printVarAndProdName(c);
		return visit(c.progbody());
	}

	@Override
	public Node visitLetInProg(LetInProgContext c) {
		if (print) printVarAndProdName(c);
		List<DecNode> declist = new ArrayList<>();
		for (CldecContext classDec : c.cldec()) declist.add((DecNode) visit(classDec));
		for (DecContext dec : c.dec()) declist.add((DecNode) visit(dec));
		return new ProgLetInNode(declist, visit(c.exp()));
	}

	@Override
	public Node visitNoDecProg(NoDecProgContext c) {
		if (print) printVarAndProdName(c);
		return new ProgNode(visit(c.exp()));
	}
	//leggo la definizione della classe e creo un nodo classe
	@Override
	public Node visitCldec(CldecContext c) {
		if (print) {
			printVarAndProdName(c);
		}
		String classID = c.ID(0).getText();
		String superID = null;
		List<FieldNode> fields = new ArrayList<>();
		//prendiamo l'id della super classe
		if (c.EXTENDS() != null) {
			superID = c.ID(1).getText();
		}
		int extendingPad = c.EXTENDS() != null ? 1 : 0;
		//nel caso in cui non c'è una super classe partiremo da 1 altrimenti da 2 poiche id 1 è della super classe
		//questo for è per i fields della classe
		IntStream.range(1 + extendingPad, c.ID().size()).forEach(i -> {
			var field = new FieldNode(c.ID(i).getText(), (TypeNode) visit(c.type(i - (1 + extendingPad))));
			field.setLine(c.ID(i).getSymbol().getLine());
			fields.add(field);
		});
		//questo è per i metodi
		List<MethodNode> methods = new ArrayList<>();
		for (var method : c.methdec()) {
			methods.add((MethodNode) visit(method));
		}
		//alla fine creo la classe
		var node = new ClassNode(classID, superID, fields, methods);
		node.setLine(c.ID(0).getSymbol().getLine());
		return node;
	}

	//leggo la definizione del metodo e creo un nodo metodo
	@Override
	public Node visitMethdec(MethdecContext c) {
		if (print) {
			printVarAndProdName(c);
		}
		String methodId = c.ID(0).getText();
		TypeNode retType = (TypeNode) visit(c.type(0));
		//mi salvo i parametri del metodo
		List<ParNode> parList = new ArrayList<>();
		IntStream.range(1, c.ID().size()).forEach(i -> {
			parList.add(new ParNode(c.ID(i).getText(), (TypeNode) visit(c.type(i))));
		});
		//mi salvo le dichiarazioni del metodo
		List<DecNode> decList = new ArrayList<>();
		for (var declaration : c.dec()) {
			decList.add((DecNode) visit(declaration));
		}
		//creo un nodo di tipologia metodo
		var node = new MethodNode(methodId, retType, parList, decList, visit(c.exp()));
		node.setLine(c.ID(0).getSymbol().getLine());
		return node;
	}
	//per quando facciamo new classe.
	@Override
	public Node visitNew(NewContext c) {
		if (print) {
			printVarAndProdName(c);
		}
		//mi salvo gli argomenti definiti
		List<Node> argList = new ArrayList<>();
		for (var i = 0; i < c.exp().size(); i++) {
			argList.add(visit(c.exp(i)));
		}
		//creo un nodo new e gli do il riferimento alla classe, tramite id, e gli argomenti definiti
		var node = new NewNode(c.ID().getText(), argList);
		node.setLine(c.ID().getSymbol().getLine());
		return node;
	}

	@Override
	public Node visitTimesDiv(TimesDivContext c) {
		if (print) printVarAndProdName(c);
		Node n = null;
		if(c.TIMES()!=null){
			//c.TIMES() per vedere se è nullo oppure no
			//la produzione con etichetta #timesDiv ha due produzioni exp e per richiamare la prima devo dargli 0 e per la seconda 1
			n = new TimesNode(visit(c.exp(0)), visit(c.exp(1)));
			//facendo l'istruzione sopra ritorno un nodo di tipo moltiplicazione che farà il primo visit * il secondo visit
			n.setLine(c.TIMES().getSymbol().getLine());		// setLine added
		} else if (c.DIV()!=null) {
			n = new DivNode(visit(c.exp(0)), visit(c.exp(1)));
			n.setLine(c.DIV().getSymbol().getLine());
		}
		return n;
	}

	@Override
	public Node visitPlusMinus(PlusMinusContext c) {
		if (print) printVarAndProdName(c);
		Node n = null;
		if(c.PLUS()!=null){
			n = new PlusNode(visit(c.exp(0)), visit(c.exp(1)));
			n.setLine(c.PLUS().getSymbol().getLine());
		} else if (c.MINUS()!=null) {
			n = new MinusNode(visit(c.exp(0)), visit(c.exp(1)));
			n.setLine(c.MINUS().getSymbol().getLine());
		}

		return n;
	}

	@Override
	public Node visitComp(CompContext c) {
		if (print) printVarAndProdName(c);
		Node n = null;
		if(c.EQ() != null){
			n = new EqualNode(visit(c.exp(0)), visit(c.exp(1)));
			n.setLine(c.EQ().getSymbol().getLine());
		} else if (c.GE() != null) {
			n = new GreaterEqualNode(visit(c.exp(0)), visit(c.exp(1)));
			n.setLine(c.GE().getSymbol().getLine());
		} else if (c.LE() != null) {
			n = new LessEqualNode(visit(c.exp(0)), visit(c.exp(1)));
			n.setLine(c.LE().getSymbol().getLine());
		}
        return n;		
	}

	@Override
	public Node visitAndOr(AndOrContext c) {
		if (print) {
			printVarAndProdName(c);
		}
		Node n;
		if (c.AND() != null) {
			n = new AndNode(visit(c.exp(0)), visit(c.exp(1)));
			n.setLine(c.AND().getSymbol().getLine());
		} else {
			n = new OrNode(visit(c.exp(0)), visit(c.exp(1)));
			n.setLine(c.OR().getSymbol().getLine());
		}
		return n;
	}

	@Override
	public Node visitNot(NotContext c) {
		if (print) {
			printVarAndProdName(c);
		}
		Node n = new NotNode(visit(c.exp()));
		n.setLine(c.NOT().getSymbol().getLine());
		return n;
	}

	//controllo se il campo ID di VAR è null e quindi incompleto
	@Override
	public Node visitVardec(VardecContext c) {
		if (print) printVarAndProdName(c);
		Node n = null;
		if (c.ID()!=null) { //non-incomplete ST
			n = new VarNode(c.ID().getText(), (TypeNode) visit(c.type()), visit(c.exp()));
			n.setLine(c.VAR().getSymbol().getLine());
		}
        return n;
	}

	@Override
	public Node visitFundec(FundecContext c) {
		if (print) printVarAndProdName(c);
		List<ParNode> parList = new ArrayList<>();
		for (int i = 1; i < c.ID().size(); i++) { 
			ParNode p = new ParNode(c.ID(i).getText(),(TypeNode) visit(c.type(i)));
			p.setLine(c.ID(i).getSymbol().getLine());
			parList.add(p);
		}
		List<DecNode> decList = new ArrayList<>();
		for (DecContext dec : c.dec()) decList.add((DecNode) visit(dec));
		Node n = null;
		if (c.ID().size()>0) { //non-incomplete ST
			n = new FunNode(c.ID(0).getText(),(TypeNode)visit(c.type(0)),parList,decList,visit(c.exp()));
			n.setLine(c.FUN().getSymbol().getLine());
		}
        return n;
	}

	@Override
	public Node visitIntType(IntTypeContext c) {
		if (print) printVarAndProdName(c);
		return new IntTypeNode();
	}

	@Override
	public Node visitBoolType(BoolTypeContext c) {
		if (print) printVarAndProdName(c);
		return new BoolTypeNode();
	}

	@Override
	public Node visitInteger(IntegerContext c) {
		if (print) printVarAndProdName(c);
		//con c.NUM() prendo il numero
		int v = Integer.parseInt(c.NUM().getText());
		return new IntNode(c.MINUS()==null?v:-v);
	}

	@Override
	public Node visitTrue(TrueContext c) {
		if (print) printVarAndProdName(c);
		return new BoolNode(true);
	}

	@Override
	public Node visitFalse(FalseContext c) {
		if (print) printVarAndProdName(c);
		return new BoolNode(false);
	}

	@Override
	public Node visitIf(IfContext c) {
		if (print) printVarAndProdName(c);
		Node ifNode = visit(c.exp(0));
		Node thenNode = visit(c.exp(1));
		Node elseNode = visit(c.exp(2));
		Node n = new IfNode(ifNode, thenNode, elseNode);
		n.setLine(c.IF().getSymbol().getLine());			
        return n;		
	}

	@Override
	public Node visitPrint(PrintContext c) {
		if (print) printVarAndProdName(c);
		return new PrintNode(visit(c.exp()));
	}

	@Override
	public Node visitPars(ParsContext c) {
		if (print) printVarAndProdName(c);
		//ritorno il figlio perché le parentesi non fanno calcolo e hanno un exp
		return visit(c.exp());
	}

	@Override
	public Node visitId(IdContext c) {
		if (print) printVarAndProdName(c);
		Node n = new IdNode(c.ID().getText());
		n.setLine(c.ID().getSymbol().getLine());
		return n;
	}

	@Override
	public Node visitIdType(IdTypeContext c) {
		if (print) {
			printVarAndProdName(c);
		}
		//ci salviamo l'id della classe come campo
		var node = new RefTypeNode(c.ID().getText());
		node.setLine(c.ID().getSymbol().getLine());
		return node;
	}

	@Override
	public Node visitCall(CallContext c) {
		if (print) printVarAndProdName(c);		
		List<Node> arglist = new ArrayList<>();
		for (ExpContext arg : c.exp()) arglist.add(visit(arg));
		Node n = new CallNode(c.ID().getText(), arglist);
		n.setLine(c.ID().getSymbol().getLine());
		return n;
	}

	@Override
	public Node visitDotCall(DotCallContext c) {
		if (print) {
			printVarAndProdName(c);
		}
		//argomenti chiamata metodo
		List<Node> arglist = new ArrayList<>();
		for (ExpContext arg : c.exp()) {
			arglist.add(visit(arg));
		}
		//nodo di tipologia dot call node.
		Node node = new ClassCallNode(c.ID(0).getText(), //id dell'ogetto
				c.ID(1).getText(), //metodo id
				arglist); //argomenti della chiamata
		node.setLine(c.ID(1).getSymbol().getLine());
		return node;
	}

	@Override
	public Node visitNull(NullContext c) {
		if (print) {
			printVarAndProdName(c);
		}
		Node n = new EmptyNode();
		n.setLine(c.NULL().getSymbol().getLine());
		return n;
	}

}
