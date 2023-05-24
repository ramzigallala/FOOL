package svm;
public class ExecuteVM {
    //qui facciamo codice assembly (il codice assembly ha estensione .asm ), non è ancora codice oggetto. Infatti è ancora testuale. Ricordiamo che un codice sorgente assembly a una sintassi banale rispetto a un codice sorgente tipo java o C. Perchè ho solo una sequenza lineare di istruzioni.
    //un assembler trasforma le istruzioni in codice oggetto, ovvero codici numerici. TODO studia i tipi di indirizzamento
    //lo stack cresce verso il basso quindi la push è -- e la pop ++
    //questa è la virtual machine
    //qui facciamo l'assembly e l'esecuzione(virtual machine)
    //per quanto riguarda le variabili, ad ognuna viene assegnato un offset durante l'assegnazione e tramite esso ci ricondiciamo all'assegnazione quando viene usata la variabile. gli offset vengono messi dentro l'STEntry
    //access link per ritrovare variabili non definite nell scope attuale. Noi abbiamo scopping statico
    public static final int CODESIZE = 10000;
    public static final int MEMSIZE = 10000;
    
    private int[] code;
    private int[] memory = new int[MEMSIZE];
    //sono registri
    private int ip = 0; //indirizzo della prossima istruzione da eseguire. istruction pointer
    private int sp = MEMSIZE; //punta al top dello stack. stack pointer
    
    private int hp = 0;       //heap pointer
    private int fp = MEMSIZE;  //frame pointer
    private int ra;           //return adress
    private int tm;           //temporary storage
    
    public ExecuteVM(int[] code) {
      this.code = code;
    }
    //fa la fetch (prendo la prosssima istruzione da eseguire) e execute
    public void cpu() {
      while ( true ) {
        int bytecode = code[ip++]; // fetch
        int v1,v2;
        int address;
        //lo switch in base codice in bytecode implementerà l'istruzione
        switch ( bytecode ) {
          case SVMParser.PUSH:
            push( code[ip++] );
            break;
          case SVMParser.POP:
            pop();
            break;
          case SVMParser.ADD :
            v1=pop();
            v2=pop();
            push(v2 + v1);
            break;
          case SVMParser.MULT :
            v1=pop();
            v2=pop();
            push(v2 * v1);
            break;
          case SVMParser.DIV :
            v1=pop();
            v2=pop();
            push(v2 / v1);
            break;
          case SVMParser.SUB :
            v1=pop();
            v2=pop();
            push(v2 - v1);
            break;
          case SVMParser.STOREW : //
            address = pop();
            memory[address] = pop();    
            break;
          case SVMParser.LOADW : //
            push(memory[pop()]);
            break;
          case SVMParser.BRANCH : 
            address = code[ip];
            ip = address;
            break;
          case SVMParser.BRANCHEQ :
            address = code[ip++];
            v1=pop();
            v2=pop();
            if (v2 == v1) ip = address;
            break;
          case SVMParser.BRANCHLESSEQ :
            address = code[ip++];
            v1=pop();
            v2=pop();
            if (v2 <= v1) ip = address;
            break;
          case SVMParser.JS : //
            address = pop();
            ra = ip;
            ip = address;
            break;
         case SVMParser.STORERA : //
            ra=pop();
            break;
         case SVMParser.LOADRA : //
            push(ra);
            break;
         case SVMParser.STORETM : 
            tm=pop();
            break;
         case SVMParser.LOADTM : 
            push(tm);
            break;
         case SVMParser.LOADFP : //
            push(fp);
            break;
         case SVMParser.STOREFP : //
            fp=pop();
            break;
         case SVMParser.COPYFP : //
            fp=sp;
            break;
         case SVMParser.STOREHP : //
            hp=pop();
            break;
         case SVMParser.LOADHP : //
            push(hp);
            break;
         case SVMParser.PRINT :
            System.out.println((sp<MEMSIZE)?memory[sp]:"Empty stack!");
            break;
         case SVMParser.HALT :
            return;
        }
      }
    } 
    
    private int pop() {
      return memory[sp++];
    }
    
    private void push(int v) {
      memory[--sp] = v;
    }
    
}