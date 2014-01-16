package org.camunda.bpm.engine.tools;

public class Execution implements Vertex {

  private String id;
  private String parent;
  private String instance;
  private boolean active;
  private boolean concurrent;
  private boolean scope;

  public Execution(String id, String parent, String instance, boolean active, boolean concurrent, boolean scope) {
    this.id = id;
    this.parent = parent;
    this.instance = instance;
    this.active = active;
  }

  public String getId() {
    return id;
  }

  public String getParent() {
    return parent;
  }

  @Override
  public String toString() {
    return "EX " + id;
  }

}
