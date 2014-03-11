/*
 * SDDependency.java
 *
 * Simple class to contain word dependency information, including
 * -The type of relation
 * -The governor word
 * -The dependent word
 */

package com.openllamatalk.helloglass;

import java.io.Serializable;

public class SDDependency implements Serializable {

  public String reln;
  public String gov;
  public String dep;

  public SDDependency(String relation, String governor, String dependent) {
    reln = relation;
    gov = governor;
    dep = dependent;
  }

}
