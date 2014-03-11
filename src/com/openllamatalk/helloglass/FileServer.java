package com.openllamatalk.helloglass;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;


public class FileServer {

  public static void main(String[] args) throws IOException {

    LexicalizedParser lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");


    int filesize = 6022386;  // filesize temporarily hardcoded.

    long start = System.currentTimeMillis();
    int bytesRead;
    int current = 0;

    // Create socket
    ServerSocket servsock = new ServerSocket(1149);
    while (true) {
      Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
      for (NetworkInterface netint : Collections.list(nets)) {
        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
          System.out.println("InetAddress: " + inetAddress);
        }
      }
      System.out.println("WAITING...");

      Socket sock = servsock.accept();
      System.out.println("ACCEPTED CONNECTION : " + sock);

      // Receive file
      byte[] myByteArray = new byte[filesize];
      InputStream is = sock.getInputStream();


      int ret = 0;
      int offset = 0;
      while ((ret = is.read(myByteArray, offset, filesize - offset)) > 0) {
        System.out.println("GOT SOMETHING");
        offset += ret;
        // Just in case the file is bigger than the buffer size.
        if (offset >= filesize)
          break;
      }
    


      byte[] finalByteArray = Arrays.copyOf(myByteArray, offset+1);
      String sentence = new String(finalByteArray, "UTF-8");
      System.out.println("THIS IS SENTENCE: " + sentence);

      // Do dep parsing on sentence.
      TokenizerFactory<CoreLabel> tokenizerFactory =
          PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
      Tokenizer<CoreLabel> tok =
          tokenizerFactory.getTokenizer(new StringReader(sentence));
      List<CoreLabel> rawWords = tok.tokenize();

      Tree parse = lp.apply(rawWords);
      TreebankLanguagePack tlp = new PennTreebankLanguagePack();
      GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
      GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
      List<TypedDependency> wordDeps = gs.typedDependencies(false);
      System.out.println("DONE WITH DEP PARSING");

      List<SDDependency> simplifiedDeps = new ArrayList<SDDependency>();
      for (TypedDependency typeDep : wordDeps) {
        SDDependency dep = new SDDependency(typeDep.reln().toString(),
                                            typeDep.gov().toString(),
                                            typeDep.dep().toString());
        simplifiedDeps.add(dep);
      }
      OutputStream socketStream = sock.getOutputStream();
      ObjectOutput objectOutput = new ObjectOutputStream(socketStream);
      objectOutput.writeObject(simplifiedDeps);

      long end = System.currentTimeMillis();
      System.out.println(end - start);

      // RESPONSE FROM SERVER
      PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
      out.println(99);  // REPLY WITH NUMBER 99

      out.close();

      sock.close();
    }
  }
}
