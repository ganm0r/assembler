package main;

import java.io.IOException;

import assembler.PassOneAssembler;

public class Main {
    public static void main(String[] args) throws IOException {
        final String INPUT_FILE_NAME = "/home/gndhrv/Documents/Sem VI/SPCC/Assembler/lib/inputFile.txt";
        final String OUTPUT_FILE_NAME = "/home/gndhrv/Documents/Sem VI/SPCC/Assembler/lib/outputFile.txt";

        PassOneAssembler passOneAssembler = new PassOneAssembler();
        passOneAssembler.buildSymbolTable(INPUT_FILE_NAME, OUTPUT_FILE_NAME);
    }
}



