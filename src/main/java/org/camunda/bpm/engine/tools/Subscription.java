package org.camunda.bpm.engine.tools;

public class Subscription implements Vertex {

  private String id;
  private String execution;
  private String type;
  private String name;

  public Subscription(String id, String execution, String type, String name) {
    this.id = id;
    this.execution = execution;
    this.type = type;
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public String getExecution() {
    return execution;
  }

  @Override
  public String toString() {
    return "ES " + id;
  }
}
