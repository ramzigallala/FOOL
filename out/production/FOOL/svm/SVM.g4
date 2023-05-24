grammar SVM;

@parser::header {
import java.util.*;
}

@lexer::members {
public int lexicalErrors=0;
}
   
@parser::members { 
public int[] code = new int[ExecuteVM.CODESIZE]; //code è praticamente la lista in cui troviamo i numeri che fanno riferimento alle istruzioni
private int i = 0;
private Map<String,Integer> labelDef = new HashMap<>(); //fa il cotrario di quello sotto e quindi quando troviamo una etichetta gli diciamo a quale indirizzo fa riferimento
private Map<Integer,String> labelRef = new HashMap<>(); // gestisce i buchi (riferimenti irrisolti) cosi quando dobiamo saltare lasciamo in buco dicendo che dovrà essere rimpito da qualcuno tipo pippo.  dopo mapperemo l'indirizzo del buco all'etichetta che andremo a mettergli sopra
//alla fine con un for popolo i labelref utilizzando i labelDEf
}

/*------------------------------------------------------------------
 * PARSER RULES
 costruisco l'assemblatore che preso il codice assembly lo trasforma in un codice oggetto testuale assembly, quindi leggermente più di alto livello del vero codice oggetto
TODO imparare i tipi di indirizzamento
assemblatore gestisce le etichette
noi prendiamo il file compilato che finisce con estensione .svm e grzie all'assemblatore lo trasformiamo in codice oggetto
"halt" fa uscire la virtual machine dall'esecuzione
per fare una operazione devo prima mettere i numeri e poi l'operatore 5+7=5 7 + gli argomenti quando fai + sono impliciti
 con questo prendiamo un file assembly e ne facciamo prima il lexer e poi il parser e infine dopo aver risolto le etichette e generiamo il codice oggetto
 posso usare PUSH perché ho già definito le costanti nel lexer
 *------------------------------------------------------------------*/
   
assembly: instruction* EOF 	{ for (Integer j: labelRef.keySet()) 
								code[j]=labelDef.get(labelRef.get(j)); 
							} ;

instruction : 
        PUSH n=INTEGER   {code[i++] = PUSH; 
			              code[i++] = Integer.parseInt($n.text);}
	  | PUSH l=LABEL    {code[i++] = PUSH; 
	    		             labelRef.put(i++,$l.text);} 		     
	  | POP		    {code[i++] = POP;}	
	  | ADD		    {code[i++] = ADD;}
	  | SUB		    {code[i++] = SUB;}
	  | MULT	    {code[i++] = MULT;}
	  | DIV		    {code[i++] = DIV;}
	  | STOREW	  {code[i++] = STOREW;} //
	  | LOADW           {code[i++] = LOADW;} //
	  | l=LABEL COL     {labelDef.put($l.text,i);}
	  | BRANCH l=LABEL  {code[i++] = BRANCH;
                       labelRef.put(i++,$l.text);}
	  | BRANCHEQ l=LABEL {code[i++] = BRANCHEQ;
                        labelRef.put(i++,$l.text);}
	  | BRANCHLESSEQ l=LABEL {code[i++] = BRANCHLESSEQ;
                          labelRef.put(i++,$l.text);}
	  | JS              {code[i++] = JS;}		     //
	  | LOADRA          {code[i++] = LOADRA;}    //
	  | STORERA         {code[i++] = STORERA;}   //
	  | LOADTM          {code[i++] = LOADTM;}   
	  | STORETM         {code[i++] = STORETM;}   
	  | LOADFP          {code[i++] = LOADFP;}   //
	  | STOREFP         {code[i++] = STOREFP;}   //
	  | COPYFP          {code[i++] = COPYFP;}   //
	  | LOADHP          {code[i++] = LOADHP;}   //
	  | STOREHP         {code[i++] = STOREHP;}   //
	  | PRINT           {code[i++] = PRINT;}
	  | HALT            {code[i++] = HALT;}
	  ;
	  
/*------------------------------------------------------------------
 * LEXER RULES
 *------------------------------------------------------------------*/

PUSH	 : 'push' ; 	
POP	 : 'pop' ; 	
ADD	 : 'add' ;  	
SUB	 : 'sub' ;	
MULT	 : 'mult' ;  	
DIV	 : 'div' ;	
STOREW	 : 'sw' ; 	
LOADW	 : 'lw' ;	
BRANCH	 : 'b' ;	
BRANCHEQ : 'beq' ;	
BRANCHLESSEQ:'bleq' ;	
JS	 : 'js' ;	
LOADRA	 : 'lra' ;	
STORERA  : 'sra' ;	 
LOADTM	 : 'ltm' ;	
STORETM  : 'stm' ;	
LOADFP	 : 'lfp' ;	
STOREFP	 : 'sfp' ;	
COPYFP   : 'cfp' ;      
LOADHP	 : 'lhp' ;	
STOREHP	 : 'shp' ;	
PRINT	 : 'print' ;	
HALT	 : 'halt' ;	
 
COL	 : ':' ;
LABEL	 : ('a'..'z'|'A'..'Z')('a'..'z' | 'A'..'Z' | '0'..'9')* ;
INTEGER	 : '0' | ('-')?(('1'..'9')('0'..'9')*) ;

COMMENT : '/*' .*? '*/' -> channel(HIDDEN) ;

WHITESP  : (' '|'\t'|'\n'|'\r')+ -> channel(HIDDEN) ;

ERR	     : . { System.out.println("Invalid char: "+getText()+" at line "+getLine()); lexicalErrors++; } -> channel(HIDDEN); 

