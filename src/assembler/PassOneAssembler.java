package assembler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

import datastructures.MOT;
import datastructures.POT;
import datastructures.SYMTAB;

public class PassOneAssembler {

    private int locationCounter;
    private Vector<POT> pot;
    private Vector<MOT> mot;
    private Vector<SYMTAB> symtab;
    private boolean isLabel;

    private void initialisePotAndMot(Vector<POT> pot, Vector<MOT> mot, Vector<SYMTAB> symtab) {
        POT p1=new POT();   p1.pseudo="START";  pot.add(p1);
        POT p2=new POT();   p2.pseudo="END";    pot.add(p2);
        POT p3=new POT();   p3.pseudo="USING";  pot.add(p3);
        POT p4=new POT();   p4.pseudo="LTORG";  pot.add(p4);
        POT p5=new POT();   p5.pseudo="DROP";   pot.add(p5);
        POT p6=new POT();   p6.pseudo="EQU";    pot.add(p6);
        POT p7=new POT();   p7.pseudo="DC";     pot.add(p7);
        POT p8=new POT();   p8.pseudo="DS";     pot.add(p8);
        
        MOT m1=new MOT();	m1.mnemonic="LA";	m1.type="RX";	m1.length=4;    mot.add(m1);
        MOT m2=new MOT();	m2.mnemonic="L";	m2.type="RX";	m2.length=4;	mot.add(m2);
        MOT m3=new MOT();	m3.mnemonic="SR";	m3.type="RR";	m3.length=2;	mot.add(m3);
        MOT m4=new MOT();	m4.mnemonic="AR";	m4.type="RR";	m4.length=2;	mot.add(m4);
        MOT m5=new MOT();	m5.mnemonic="A";	m5.type="RX";	m5.length=4;	mot.add(m5);
        MOT m6=new MOT();	m6.mnemonic="LR";	m6.type="RR";	m6.length=2;	mot.add(m6);
        MOT m7=new MOT();	m7.mnemonic="BR";	m7.type="RR";	m7.length=2;    mot.add(m7);
        MOT m8=new MOT();	m8.mnemonic="C";	m8.type="RX";	m8.length=4;    mot.add(m8);
        MOT m9=new MOT();	m9.mnemonic="BNE";	m9.type="RX";	m9.length=4;    mot.add(m9);
        MOT m10=new MOT();	m10.mnemonic="ST";	m10.type="RX";	m10.length=4;   mot.add(m10);

        for(int i = 0; i < (this.pot.size()); i++) {
            for(int j = 0; j < (this.pot.size()); j++) {
                POT tempPot1 = pot.elementAt(i);
                POT tempPot2 = pot.elementAt(j);
                POT tempPot = new POT();

                if(((tempPot1.pseudo).compareTo(tempPot2.pseudo)) < 0) {
                    tempPot.pseudo = tempPot1.pseudo;
                    tempPot1.pseudo = tempPot2.pseudo;
                    tempPot2.pseudo = tempPot.pseudo;
                }
            }
        }

        for(int i = 0; i < (this.mot.size()); i++) {
            for(int j = 0; j < (this.mot.size()); j++) {
                MOT tempMot1 = mot.elementAt(i);
                MOT tempMot2 = mot.elementAt(j);
                MOT tempMot = new MOT();

                if(((tempMot1.mnemonic).compareTo(tempMot2.mnemonic)) < 0) {
                    tempMot.mnemonic=tempMot1.mnemonic;       tempMot1.mnemonic=tempMot2.mnemonic;     tempMot2.mnemonic=tempMot.mnemonic;
    	  			tempMot.type=tempMot1.type;               tempMot1.type=tempMot2.type;	           tempMot2.type=tempMot.type;
    	  			tempMot.length=tempMot1.length;		      tempMot1.length=tempMot2.length;		   tempMot2.length=tempMot.length;
                }
            }
        }
    }

    public PassOneAssembler() {
        this.locationCounter = 0;
        this.mot = new Vector<MOT>();
        this.pot = new Vector<POT>();
        this.symtab = new Vector<SYMTAB>();
        this.initialisePotAndMot(this.pot, this.mot, this.symtab);
        this.isLabel = false;
    }

    private static int searchPot(Vector<POT> pot, int firstIndex, int lastIndex, String token) {
        if(lastIndex >= firstIndex) {
            int middleIndex = firstIndex + (lastIndex - firstIndex) / 2;
            POT tempPot = pot.elementAt(middleIndex);

            if((tempPot.pseudo).equals(token)) {
                return middleIndex;
            }

            if(token.compareTo(tempPot.pseudo) < 0) {
                return searchPot(pot, firstIndex, middleIndex - 1, token);
            }

            return searchPot(pot, middleIndex + 1, lastIndex, token);
        }

        return -1;
    }

    private static int searchMot(Vector<MOT> mot, int firstIndex, int lastIndex, String token) {
        if(lastIndex >= firstIndex) {
            int middleIndex = firstIndex + (lastIndex - firstIndex) / 2;
            MOT tempMot = mot.elementAt(middleIndex);

            if((tempMot.mnemonic).equals(token)) {
                return middleIndex;
            }

            if(token.compareTo(tempMot.mnemonic) < 0) {
                return searchMot(mot, firstIndex, middleIndex - 1, token);
            }

            return searchMot(mot, middleIndex + 1, lastIndex, token);
        }

        return -1;
    }

    public void buildSymbolTable(String sourceCodeFile, String symbolTableFile) throws IOException, FileNotFoundException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(sourceCodeFile));

        String currentLine;
        int tokenCounter, instructionLength = 0;

        while((currentLine = bufferedReader.readLine()) != null) {
            tokenCounter = 0;

            StringTokenizer stringTokenizer = new StringTokenizer(currentLine, " ", false);
            String tokens[] = new String[stringTokenizer.countTokens()];
            String opcode = "";
            isLabel = false;

            while(stringTokenizer.hasMoreTokens()) {
                tokens[tokenCounter] = stringTokenizer.nextToken();
                tokenCounter++;
            }

            if(tokens.length == 1 || tokens.length == 2) {
                opcode += tokens[0];
            } else if(tokens.length == 3) {
                isLabel = true;

                opcode += tokens[1];
            }

            int opcodeIndexInPot = searchPot(pot, 0, pot.size() - 1, opcode);

            if(opcodeIndexInPot == -1) {
                int opcodeIndexInMot = searchMot(mot, 0, mot.size() - 1, opcode);

                if(!(opcodeIndexInMot == -1)) {
                    MOT tempMot = mot.elementAt(opcodeIndexInMot);
                
                    instructionLength = tempMot.length;
    
                    if(isLabel) {
                        SYMTAB tempSymtab = new SYMTAB();
                        tempSymtab.symbol = tokens[0];
                        tempSymtab.value = this.locationCounter;
                        tempSymtab.length = instructionLength;
    
                        this.symtab.add(tempSymtab);
                    }
    
                    this.locationCounter = this.locationCounter + instructionLength;
                }
            } else {
                if(opcode.equals("START")) {
                    this.locationCounter = 0;
                    if(isLabel) {
                        SYMTAB tempSymtab = new SYMTAB();
                        tempSymtab.symbol = tokens[0];
                        tempSymtab.value = this.locationCounter;
                        tempSymtab.length = 1;
                        tempSymtab.relocation = 'R';

                        this.symtab.add(tempSymtab);
                    }
                } else if(opcode.equals("DS") || opcode.equals("DC")) {
                    
                    while(this.locationCounter % 4 != 0) {
                        this.locationCounter++;
                    }

                    if(isLabel) {
                        SYMTAB tempSymtab = new SYMTAB();
                        tempSymtab.symbol = tokens[0];
                        tempSymtab.value = this.locationCounter;
                        tempSymtab.length = 4;
                        tempSymtab.relocation = 'R';

                        this.symtab.add(tempSymtab);
                    }

                    this.locationCounter = this.locationCounter + instructionLength;
                } else if(opcode.equals("EQU")) {
                    SYMTAB tempSymtab = new SYMTAB();
                    tempSymtab.symbol = tokens[0];
                    tempSymtab.length = 1;
                    tempSymtab.relocation = 'A';

                    if(tokens[2] == "*") {
                        tempSymtab.value = this.locationCounter;
                    } else {
                        tempSymtab.value = Integer.parseInt(tokens[2]);
                    }

                    this.symtab.add(tempSymtab);
                }
            }
        }

        bufferedReader.close();

        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(symbolTableFile));

        bufferedWriter.write("Symbol  Value  Length  Relocation");
        bufferedWriter.newLine();

        for(int i = 0; i < this.symtab.size(); i++) {
            SYMTAB tempSymtab = this.symtab.elementAt(i);

            bufferedWriter.write(tempSymtab.symbol+"       "+tempSymtab.value+"       "+tempSymtab.length+"      "+tempSymtab.relocation);
            bufferedWriter.newLine();
        }

        bufferedWriter.close();
    }
}
