package compiler;

import compiler.AST.*;
import compiler.lib.*;
import compiler.exc.*;
import svm.ExecuteVM;

import java.util.ArrayList;
import java.util.List;

import static compiler.lib.FOOLlib.*;

public class CodeGenerationASTVisitor extends BaseASTVisitor<String, VoidException> {

	List<List<String>> dispatchTables = new ArrayList<>();
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
	public String visitNode(LessEqualNode n) {
		if (print) printNode(n);
		//generiamo una etichetta. non possiamo dare lo stesso nome
		String l1 = freshLabel();
		String l2 = freshLabel();
		return nlJoin(
				visit(n.left),
				visit(n.right),
				"bleq "+l1,
				"push 0",
				"b "+l2,
				l1+":",
				"push 1",
				l2+":"
		);
	}
	//rispetto alla precedente scambio il then con l'else
	@Override
	public String visitNode(GreaterEqualNode n) {
		if (print) printNode(n);
		//generiamo una etichetta. non possiamo dare lo stesso nome
		String l1 = freshLabel();
		String l2 = freshLabel();
		return nlJoin(
				visit(n.right),
				visit(n.left),
				"bleq "+l1,
				"push 0",
				"b "+l2,
				l1+":",
				"push 1",
				l2+":"
		);
	}

	@Override
	public  String visitNode(OrNode n){
		if (print) printNode(n);
		String l1 = freshLabel();
		String l2 = freshLabel();
		return nlJoin(
				visit(n.left),
				"push 1",
				"beq "+l1,
				visit(n.right),
				"push 1",
				"beq "+l1,
				"push 0",
				"b "+l2,
				l1+":",
				"push 1",
				l2+":"
		);
	}

	@Override
	public  String visitNode(AndNode n){
		if (print) printNode(n);
		String l1 = freshLabel();
		String l2 = freshLabel();
		//
		return nlJoin(
				visit(n.left),
				"push 0",
				"beq "+l1,
				visit(n.right),
				"push 0",
				"beq "+l1, //se è falso faccio push di 1 altrimenti salto a l1 e faccio push di 0
				"push 1",
				"b "+l2,
				l1+":",
				"push 0",
				l2+":"
		);
	}

	@Override
	public String visitNode(NotNode n){
		if (print) printNode(n);
		String l1 = freshLabel();
		String l2 = freshLabel();
		//nego l'espressione quindi se è uguale a 0 allora andrò all'etichetta l1 e faccio push di 1 altrimenti di 0
		return nlJoin(
				visit(n.expression),
				"push 0",
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
	public String visitNode(DivNode n) {
		if (print) printNode(n);
		return nlJoin(
				visit(n.left),
				visit(n.right),
				"div"
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
	public String visitNode(MinusNode n) {
		if (print) printNode(n);
		return nlJoin(
				visit(n.left),
				visit(n.right),
				"sub"
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

	//object implementatio

	@Override
	public String visitNode(ClassNode node) {
		if (print) {
			printNode(node, node.id);
		}
		List<String> dispatchTable = new ArrayList<>();
		dispatchTables.add(dispatchTable);
		//aggiungiamo tutti gli indirizzi relativi anche alla super classe
		if (node.superID != null) {
			var superClassDispatchTable = dispatchTables.get(-node.superClassEntry.offset-2);
			dispatchTable.addAll(superClassDispatchTable);
		}
		//inseriamo nella dispatch table tutti i metodi
		for (int i = 0; i < node.methods.size(); i++) {
			var method = node.methods.get(i);
			visit(method);
			//controlliamo se il metodo è di override o meno
			if (method.offset < dispatchTable.size()) {
				//è un overrride allora cambiamo la label
				dispatchTable.set(method.offset, method.label);
			} else {
				//non è override allora aggiungiamo un nuovo campo alla dispatch table
				dispatchTable.add(method.offset, method.label);
			}
		}
		String createDispatchTable = null;
		for (String label : dispatchTable) {
			createDispatchTable = nlJoin(
					createDispatchTable,
					"push " + label, //metto nello stack l'etichetta
					"lhp", //metto nello stack il valore di hp
					"sw", // fa la pop dei due elementi e poi va a mettere nell'indirizzo puntato da hp l'etichetta
					"lhp", //da qui in poi faccio l'incremento di hp. difatti qui lo metto nello stack
					"push 1", //metto nello stack 1
					"add", //faccio la somma
					"shp" //aggiorno il valore di hp
			);
		}
		return nlJoin(
				"lhp", // metto hp sullo stack, ovvero il dispatch pointer da ritornare alla fine. Questo mi serve perché io sotto metto a partire da questo hp nella memoria le etichette. alla fine grazie a questo hp io so dopo da dove partire nella memoria(memory[hp]) a vedere quali sono le etichette di una determinata classe.
				createDispatchTable // creo sullo heap la dispatch table
		);
	}

	//è come fun node però assegniamo al nodo la label e abbiamo un ritorno null
	@Override
	public String visitNode(MethodNode node) {
		if (print) {
			printNode(node, node.id);
		}
		String declarationListCode = null;
		String popDeclarationsList = null;
		for (Node declaration : node.declarationsList) {
			declarationListCode = nlJoin(declarationListCode, visit(declaration));
			popDeclarationsList = nlJoin(popDeclarationsList, "pop");
		}
		String popParametersList = null;
		for (int i = 0; i < node.parametersList.size(); i++) {
			popParametersList = nlJoin(popParametersList, "pop");
		}

		String functionLabel = freshFunLabel();
		node.label = functionLabel;
		putCode(
				nlJoin(
						functionLabel + ":",
						"cfp", // set $fp to $sp value
						"lra", // load $ra value
						declarationListCode, // generate code for local declarations (they use the new $fp!!!)
						visit(node.expression), // generate code for function body expression
						"stm", // set $tm to popped value (function result)
						popDeclarationsList, // remove local declarations from stack
						"sra", // set $ra to popped value
						"pop", // remove Access Link from stack
						popParametersList, // remove parameters from stack
						"sfp", // set $fp to popped value (Control Link)
						"ltm", // load $tm value (function result)
						"lra", // load $ra value
						"js"  // jump to to popped address
				)
		);
		return null;
	}

	@Override
	public String visitNode(EmptyNode node) {
		if (print) {
			printNode(node);
		}
		return "push -1"; //cosi so che è sicuramente diverso da object pointer di ogni oggetto creato
	}

	@Override
	public String visitNode(CallNode n) {
		if (print) printNode(n,n.id);
		String argCode = null, getAR = null; //getActivationRecordCode
		for (int i=n.arglist.size()-1;i>=0;i--) argCode=nlJoin(argCode,visit(n.arglist.get(i))); //cosi avrò che cresce verso l'alto. e quindi li visito in ordine inverso
		for (int i = 0;i<n.nl-n.entry.nl;i++) getAR=nlJoin(getAR,"lw"); //cosi arrivo alla dichiarazione della funzione
		String commonCode = nlJoin(
				"lfp", // load Control Link (pointer to frame of function "id" caller)
				argCode, // generate code for argument expressions in reversed order
				"lfp", getAR, // retrieve address of frame containing "id" declaration
				// by following the static chain (of Access Links)
				"stm", // set $tm to popped value (with the aim of duplicating top of stack)
				"ltm", // load Access Link (pointer to frame of function "id" declaration)
				"ltm" // duplicate top of stack
		);
		if (n.entry.type instanceof MethodTypeNode) {
			commonCode = nlJoin(commonCode, "lw"); // carico il valore del dispatchpointer. Quindi carico l'indirizzo della classe
		}
		return nlJoin(commonCode,
				"push " + n.entry.offset, "add", // compute address of "id" declaration, method or fuction
				"lw", // load address of "id" method/function
				"js"  // jump to popped address (saving address of subsequent instruction in $ra)
		);
	}

	@Override
	public String visitNode(NewNode node) {
		if (print) {
			printNode(node, node.id);
		}
		String putArgumentsOnStack = null;
		for(var argument : node.argumentsList) {
			putArgumentsOnStack = nlJoin(
					putArgumentsOnStack,
					visit(argument)
			);
		}
		String loadArgumentsOnHeap = null;
		for (var i = 0; i < node.argumentsList.size(); i++) {
			loadArgumentsOnHeap = nlJoin(
					loadArgumentsOnHeap,
					"lhp",
					"sw", //mette dentro l'indirizzo di memoria puntato da hp il valore degli argomenti
					"lhp", //da qui in poi incremento hp
					"push 1",
					"add",
					"shp"
			);
		}
		return nlJoin(
				putArgumentsOnStack,
				loadArgumentsOnHeap,
				"push " + ExecuteVM.MEMSIZE,
				"push " + node.classSymbolTableEntry.offset,
				"add",
				"lw", // get dispatch pointer
				"lhp",
				"sw", //scrive nell'indirizzo di hp il dispatch pointer
				"lhp", //carica sullo stack il valore dello heap. è l'indirizzo del dispatch pointer da ritornare)
				"lhp", //incremento il valore dello heap
				"push 1",
				"add",
				"shp"
		);
	}

	@Override
	public String visitNode(ClassCallNode n) {
		if (print) printNode(n,n.objectId + "." + n.methodId);

		String argCode = null, getAR = null;
		for (int i=n.argumentsList.size()-1;i>=0;i--) {
			argCode = nlJoin(argCode, visit(n.argumentsList.get(i)));
		}
		for (int i = 0;i<n.nestingLevel-n.symbolTableEntry.nl;i++) {
			getAR=nlJoin(getAR,"lw"); // recupero object pointer risalendo della differenza di nl
		}

		return nlJoin(
				"lfp", // load Control Link (pointer to frame of function "id" caller)
				argCode, // generate code for argument expressions in reversed order
				"lfp", getAR, // retrieve address of frame containing "id" declaration
				// by following the static chain (of Access Links)
				"push "+n.symbolTableEntry.offset, "add", // compute address of "id" declaration
				"lw", // load address of "id" function
				"stm", // set $tm to popped value (with the aim of duplicating top of stack)
				"ltm", // load Access Link (pointer to frame of function "id" declaration)
				"ltm", // duplicate top of stack
				"lw", //
				"push "+n.methodEntry.offset, "add", // compute address of method declaration
				"lw", // load address of "id" method
				"js"  // jump to popped address (saving address of subsequent instruction in $ra)
		);
	}
}