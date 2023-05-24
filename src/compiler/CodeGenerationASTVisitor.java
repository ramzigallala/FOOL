package compiler;

import compiler.AST.*;
import compiler.lib.*;
import compiler.exc.*;
import static compiler.lib.FOOLlib.*;

public class CodeGenerationASTVisitor extends BaseASTVisitor<String, VoidException> {

  CodeGenerationASTVisitor() {}
  CodeGenerationASTVisitor(boolean debug) {super(false,debug);} //enables print for debugging
	//caso con variabili eccetera
	@Override
	public String visitNode(ProgLetInNode n) {
		if (print) printNode(n);
		String declCode = null;
		//prima di fare l'halt riempo l'activation reconrdo con le varie dichiarazioni tipo le variabili
		for (Node dec : n.declist) declCode=nlJoin(declCode,visit(dec));
		return nlJoin(
			"push 0",	//è il return address fittizio
			declCode, // generate code for declarations (allocation)			
			visit(n.exp),
			"halt",
			getCode() //è il codice in qui avremo nome funzione e il suo codice. Fa la stessa cosa per tutte le funzioni
		);
	}
	//caso in cui abbiamo solo un espressione senza variabili eccetera
	@Override
	public String visitNode(ProgNode n) {
		if (print) printNode(n);
		return nlJoin(
			visit(n.exp),
			"halt"
		);
	}

	@Override
	public String visitNode(FunNode n) {
		if (print) printNode(n,n.id);
		String declCode = null, popDecl = null, popParl = null;
		for (Node dec : n.declist) {
			declCode = nlJoin(declCode,visit(dec));
			popDecl = nlJoin(popDecl,"pop");
		}
		for (int i=0;i<n.parlist.size();i++) popParl = nlJoin(popParl,"pop");
		String funl = freshFunLabel(); //cosi dopo l'halt c'è il codice della funzione con questa etichetta
		putCode( //mi genera il codice relativo alla etichetta
			nlJoin(
				funl+":",
				"cfp", // set $fp to $sp value
				"lra", // load $ra value
				declCode, // generate code for local declarations (they use the new $fp!!!)
				visit(n.exp), // generate code for function body expression
				"stm", // set $tm to popped value (function result)
				popDecl, // remove local declarations from stack
				"sra", // set $ra to popped value
				"pop", // remove Access Link from stack
				popParl, // remove parameters from stack
				"sfp", // set $fp to popped value (Control Link)
				"ltm", // load $tm value (function result)
				"lra", // load $ra value
				"js"  // jump to to popped address
			)
		);
		return "push "+funl;		
	}

	@Override
	public String visitNode(VarNode n) {
		if (print) printNode(n,n.id);
		return visit(n.exp);
	}

	@Override
	public String visitNode(PrintNode n) {
		if (print) printNode(n);
		return nlJoin(
			visit(n.exp),
			"print"
		);
	}

	@Override
	public String visitNode(IfNode n) {
		if (print) printNode(n);
	 	String l1 = freshLabel();
	 	String l2 = freshLabel();		
		return nlJoin(
			visit(n.cond),
			"push 1",
			"beq "+l1,
			visit(n.el),
			"b "+l2,
			l1+":",
			visit(n.th),
			l2+":"
		);
	}
	//se l'uguale ritorna verso dobbiamo mettere il booleano true nell'altro caso il booleano falso entrambi sotto forma di intero.
	// Per mettere i valori deve saltare a una etichetta che noi settiamo
	//"b" è un branch incondizionato per o una o l'altra istruzione
	@Override
	public String visitNode(EqualNode n) {
		if (print) printNode(n);
		//generiamo una etichetta. non possiamo dare lo stesso nome
	 	String l1 = freshLabel();
	 	String l2 = freshLabel();
		return nlJoin(
			visit(n.left),
			visit(n.right),
			"beq "+l1,
			"push 0",
			"b "+l2,
			l1+":",
			"push 1",
			l2+":"
		);
	}

	@Override
	public String visitNode(TimesNode n) {
		if (print) printNode(n);
		return nlJoin(
			visit(n.left),
			visit(n.right),
			"mult"
		);	
	}

	@Override
	public String visitNode(PlusNode n) {
		if (print) printNode(n);
		return nlJoin(
			visit(n.left),
			visit(n.right),
			"add"				
		);
	}

	@Override
	public String visitNode(CallNode n) {
		if (print) printNode(n,n.id);
		String argCode = null, getAR = null;
		for (int i=n.arglist.size()-1;i>=0;i--) argCode=nlJoin(argCode,visit(n.arglist.get(i))); //cosi avrò che cresce verso l'alto. e quindi li visito in ordine inverso
		for (int i = 0;i<n.nl-n.entry.nl;i++) getAR=nlJoin(getAR,"lw"); //cosi arrivo alla dichiarazione della funzione
		return nlJoin(
			"lfp", // load Control Link (pointer to frame of function "id" caller)
			argCode, // generate code for argument expressions in reversed order
			"lfp", getAR, // retrieve address of frame containing "id" declaration
                          // by following the static chain (of Access Links)
            "stm", // set $tm to popped value (with the aim of duplicating top of stack)
            "ltm", // load Access Link (pointer to frame of function "id" declaration) //prima copia
            "ltm", // duplicate top of stack //seconda copia di tm
            "push "+n.entry.offset, "add", // compute address of "id" declaration
			"lw", // load address of "id" function
            "js"  // jump to popped address (saving address of subsequent instruction in $ra)
		);
	}
//è sostanzialmente l'uso della variabile definita tramite varNode
	//prendo il nesting level di dove è usata la variabile e dopo arrivo al nesting level di dove è dichiarata e infine guardo l'offset per sapere la posizione nel nesting level. risalgo attraverso la catena degli access link. fp punta all'access link
	@Override
	public String visitNode(IdNode n) {
		if (print) printNode(n,n.id);
		String getAR = null;
		for (int i = 0;i<n.nl-n.entry.nl;i++) getAR=nlJoin(getAR,"lw"); //calcolo quanti lw devo fare per arrivare alla dichiarazione della variabile
		return nlJoin(
			"lfp", getAR, // mette il valore del frame pointer sul registro poi torna indietro per trovare la dichiarazione della variabile. per fare questo fare tanti lw quanto deve risalire
			              // by following the static chain (of Access Links)
			"push "+n.entry.offset, "add", // compute address of "id" declaration
			"lw" // load value of "id" variable
		);
	}

	@Override
	public String visitNode(BoolNode n) {
		if (print) printNode(n,n.val.toString());
		return "push "+(n.val?1:0);
	}

	@Override
	public String visitNode(IntNode n) {
		if (print) printNode(n,n.val.toString());
		return "push "+n.val;
	}
}