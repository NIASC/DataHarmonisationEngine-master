package org.five_v_analytics;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
 
//TODO: must be integrated in the main code

public class MultipleCharsetDetector {
 
    public Charset detectCharset(File f, String[] charsets) {
 
        Charset charset = null;
 
        for (String charsetName : charsets) {
            charset = detectCharset(f, Charset.forName(charsetName));
            if (charset != null) {
                break;
            }
        }
 
        return charset;
    }
 
    private Charset detectCharset(File f, Charset charset) {
        try {
            BufferedInputStream input = new BufferedInputStream(new FileInputStream(f));
 
            CharsetDecoder decoder = charset.newDecoder();
            decoder.reset();
 
            byte[] buffer = new byte[512];
            boolean identified = false;
            while ((input.read(buffer) != -1) && (!identified)) {
                identified = identify(buffer, decoder);
            }
 
            input.close();
 
            if (identified) {
                return charset;
            } else {
                return null;
            }
 
        } catch (Exception e) {
            return null;
        }
    }
 
    private boolean identify(byte[] bytes, CharsetDecoder decoder) {
        try {
            decoder.decode(ByteBuffer.wrap(bytes));
        } catch (CharacterCodingException e) {
            return false;
        }
        return true;
    }
 
    
    public static void main(String[] args) throws IOException {
        File f = new File(args[0]);
        
        BufferedWriter output = null;
        output = new BufferedWriter(new FileWriter(args[1]));
 
        String[] charsetsToBeTested = {"IBM00858","IBM437","IBM775","IBM850","IBM852","IBM855","IBM857","IBM862","IBM866","ISO-8859-1","ISO-8859-2","ISO-8859-4","ISO-8859-5","ISO-8859-7","ISO-8859-9","ISO-8859-13","ISO-8859-15","KOI8-R","KOI8-U","US-ASCII","UTF-8","UTF-16","UTF-16BE","UTF-16LE","UTF-32","UTF-32BE","UTF-32LE","x-UTF-32BE-BOM","x-UTF-32LE-BOM","windows-1250","windows-1251","windows-1252","windows-1253","windows-1254","windows-1257","x-IBM737","x-IBM874","x-UTF-16LE-BOM"};
 
        MultipleCharsetDetector cd = new MultipleCharsetDetector();
        Charset charset = cd.detectCharset(f, charsetsToBeTested);
 
        if (charset != null) {
            try {
                InputStreamReader reader = new InputStreamReader(new FileInputStream(f), charset);
                int c = 0;
                while ((c = reader.read()) != -1) {
                    //System.out.print((char)c);
                    output.write((char)c);
                }
                reader.close();
                output.close();
            } catch (FileNotFoundException fnfe) {
                fnfe.printStackTrace();
            }catch(IOException ioe){
                ioe.printStackTrace();
            }
 
        }else{
            System.out.println("Unrecognized charset.");
        }
    }
}