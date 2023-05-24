grammar FOOL;
//variabile che viene incrementata ogni volta che c'è un errore
@lexer::members {
public int lexicalErrors=0;
}
   
/*------------------------------------------------------------------
 * PARSER RULES
gestiamo la grammatica
Data una grammatica dobbiamo: Disambiguare la grammatica(dando priorità), eliminare la ricorsione a sinisitra e fare left factoring(si fa solo se le produzioni hanno primi caratteri comuni)(si aggiunge una variabile per le parti in comune. spiegato malissimo. lab01 minuto 2.02.00).Questo perché usiamo l'approccio TOP-down
le variabili sono in minuscolo e.g. prog è una variabile
E -> T + C diventa term1 : term2 PLUS term3 (PLUS match con il token definito nel parser) epsilon si fa non mettendo nulla dopo la |. term2 sarà chiamato term2(). ogni barra è un case dello switch. il PLUS sarà match(PLUS)
 grammatica delle produzioni ottenuta per trasformazioni associative a sinistra
 Con ANTLR4 possiamo usare il fromato esteso EBNF e quindi usare la stella di kleeni (*)
 Con ANTLR possiamo definire la presenza opzionale di una produzione in un an'altra produzione definendolo con il "?"
 ANTLR4 elimina lui la ricorsione a sinistra diretta se presente
 ANTLR4 gestisce anche le grammatiche ambigue dandogli delle dichiarazioni cosi lui crea l'albero che rispetta tali dichiarazioni (ovvero priorità e associatività)
 *------------------------------------------------------------------*/
  
prog : progbody EOF ; //EOF cosi va fino in fondo al file

progbody : LET ( cldec+ dec* | dec+ ) IN exp SEMIC #letInProg
         | exp SEMIC                               #noDecProg
         ;

cldec  : CLASS ID (EXTENDS ID)?
              LPAR (ID COLON type (COMMA ID COLON type)* )? RPAR
              CLPAR
                   methdec*
              CRPAR ;

methdec : FUN ID COLON type
              LPAR (ID COLON type (COMMA ID COLON type)* )? RPAR
                   (LET dec+ IN)? exp
              SEMIC ;

dec : VAR ID COLON type ASS exp SEMIC #vardec
    | FUN ID COLON type
          LPAR (ID COLON type (COMMA ID COLON type)* )? RPAR
               (LET dec+ IN)? exp
          SEMIC #fundec
    ;

exp     : exp (TIMES | DIV) exp #timesDiv
        | exp (PLUS | MINUS) exp #plusMinus
        | exp (EQ | GE | LE) exp #comp
        | exp (AND | OR) exp #andOr
	    | NOT exp #not
        | LPAR exp RPAR #pars
    	| MINUS? NUM #integer
	    | TRUE #true
	    | FALSE #false
	    | NULL #null
	    | NEW ID LPAR (exp (COMMA exp)* )? RPAR #new
	    | IF exp THEN CLPAR exp CRPAR ELSE CLPAR exp CRPAR #if
	    | PRINT LPAR exp RPAR #print
        | ID #id
	    | ID LPAR (exp (COMMA exp)* )? RPAR #call
	    | ID DOT ID LPAR (exp (COMMA exp)* )? RPAR #dotCall
        ;


type    : INT #intType
        | BOOL #boolType
 	    | ID #idType
 	    ;

/*------------------------------------------------------------------
 * LEXER RULES
  troviamo le espressioni regolari
  ogni token ha una priorità. Sono già ordinato dal token con priorità dalla più alta alla più bassa
   *------------------------------------------------------------------*/

PLUS  	: '+' ;
MINUS   : '-' ;
TIMES   : '*' ;
DIV 	: '/' ;
LPAR	: '(' ;
RPAR	: ')' ;
CLPAR	: '{' ;
CRPAR	: '}' ;
SEMIC 	: ';' ;
COLON   : ':' ;
COMMA	: ',' ;
DOT	    : '.' ;
OR	    : '||';
AND	    : '&&';
NOT	    : '!' ;
GE	    : '>=' ;
LE	    : '<=' ;
EQ	    : '==' ;
ASS	    : '=' ;
TRUE	: 'true' ;
FALSE	: 'false' ;
IF	    : 'if' ;
THEN	: 'then';
ELSE	: 'else' ;
PRINT	: 'print' ;
LET     : 'let' ;
IN      : 'in' ;
VAR     : 'var' ;
FUN	    : 'fun' ;
CLASS	: 'class' ;
EXTENDS : 'extends' ;
NEW 	: 'new' ;
NULL    : 'null' ;
INT	    : 'int' ;
BOOL	: 'bool' ;
NUM     : '0' | ('1'..'9')('0'..'9')* ;

ID  	: ('a'..'z'|'A'..'Z')('a'..'z' | 'A'..'Z' | '0'..'9')* ;


WHITESP  : ( '\t' | ' ' | '\r' | '\n' )+    -> channel(HIDDEN) ; // la freccia significa che quando il lexer lo matcherà non lo passerà al parser

COMMENT : '/*' .*? '*/' -> channel(HIDDEN) ; //il ? fa in modo che non sia greedy * e.g. 4+ /*pi*/5/*fg*/+9 con il ? viene 4+5+9 se no 4++9. Quindi disabilito regola maximal match

ERR   	 : . { System.out.println("Invalid char: "+ getText() +" at line "+getLine()); lexicalErrors++; } -> channel(HIDDEN);
//l'errore é messo infondo perché se non matcha niente allora sarà un errore e non deve essere passsato al parser tutto questo quindi mettiamo la freccia

