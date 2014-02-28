/*
 * DepEnum.java
 *
 * Class for enum to string conversion of Stanford's word dependency types.
 */

package com.openllamatalk.helloglass;

public class DepEnum {

  public enum Dep {
    ABBREV,
    ADVMOD,
    AMOD,
    DEP,
    DET,
    MEASURE,
    NEG,
    NN,
    NUM,
    NUMBER,
    POSS,
    PREDET,
    PREP,
    QUANTMOD,
    REF,
    IOBJ,
    PARATAXIS,
    POBJ,
    APPOS,
    POSSESSIVE,
    PRT,
    AUX,
    TMOD,
    ADVCL,
    CSUBJ,
    PCOMP,
    RCMOD,
    COMPLM,
    EXPL,
    MARK,
    OTHERDEP;
  }

  public static Dep fromString(String stringDep) {
    if (stringDep.equals("abbrev"))
      return Dep.ABBREV;
    else if (stringDep.equals("advmod"))
      return Dep.ADVMOD;
    else if (stringDep.equals("amod"))
      return Dep.AMOD;
    else if (stringDep.equals("dep"))
      return Dep.DEP;
    else if (stringDep.equals("det"))
      return Dep.DET;
    else if (stringDep.equals("measure"))
      return Dep.MEASURE;
    else if (stringDep.equals("neg"))
      return Dep.NEG;
    else if (stringDep.equals("nn"))
      return Dep.NN;
    else if (stringDep.equals("num"))
      return Dep.NUM;
    else if (stringDep.equals("number"))
      return Dep.NUMBER;
    else if (stringDep.equals("poss"))
      return Dep.POSS;
    else if (stringDep.equals("predet"))
      return Dep.PREDET;
    else if (stringDep.equals("prep"))
      return Dep.PREP;
    else if (stringDep.equals("quantmod"))
      return Dep.QUANTMOD;
    else if (stringDep.equals("ref"))
      return Dep.REF;
    else if (stringDep.equals("iobj"))
      return Dep.IOBJ;
    else if (stringDep.equals("parataxis"))
      return Dep.PARATAXIS;
    else if (stringDep.equals("pobj"))
      return Dep.POBJ;
    else if (stringDep.equals("appos"))
      return Dep.APPOS;
    else if (stringDep.equals("possessive"))
      return Dep.POSSESSIVE;
    else if (stringDep.equals("prt"))
      return Dep.PRT;
    else if (stringDep.equals("aux"))
      return Dep.AUX;
    else if (stringDep.equals("tmod"))
      return Dep.TMOD;
    else if (stringDep.equals("advcl"))
      return Dep.ADVCL;
    else if (stringDep.equals("csubj"))
      return Dep.CSUBJ;
    else if (stringDep.equals("pcomp"))
      return Dep.PCOMP;
    else if (stringDep.equals("rcmod"))
      return Dep.RCMOD;
    else if (stringDep.equals("complm"))
      return Dep.COMPLM;
    else if (stringDep.equals("expl"))
      return Dep.EXPL;
    else if (stringDep.equals("mark"))
      return Dep.MARK;
    else
      return Dep.OTHERDEP;
  }

}
