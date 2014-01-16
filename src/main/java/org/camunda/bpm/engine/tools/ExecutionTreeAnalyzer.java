package org.camunda.bpm.engine.tools;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import org.apache.commons.collections15.functors.ConstantTransformer;

import javax.swing.*;
import java.awt.Dimension;
import java.awt.Color;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class ExecutionTreeAnalyzer {

  private static Connection connection;

  public ExecutionTreeAnalyzer() {
    try {
      Class.forName("org.h2.Driver");
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public static void main(String[] args) {
    ExecutionTreeAnalyzer analyzer = new ExecutionTreeAnalyzer();
    analyzer.connectLocalTcp();

    Forest<Vertex, String> graph = analyzer.createGraph(//
      analyzer.getParentExecutions(), //
      analyzer.getChildExecutions(), //
      analyzer.getEventSubscriptions());
    analyzer.closeConnection();

    analyzer.renderGraph(graph);
  }

  private Forest<Vertex, String> createGraph(List<Execution> parentExecutions, List<Execution> childExecutions, List<Subscription> eventSubscriptions) {
    Forest<Vertex, String> graph = new DelegateForest<Vertex, String>();
    HashMap<String, Vertex> vertices = new HashMap<String, Vertex>();
    for (Execution parent : parentExecutions) {
      System.out.println("Parent " + parent);
      graph.addVertex(parent);
      vertices.put(parent.getId(), parent);
    }
    for (Execution child: childExecutions) {
      System.out.println("Child " + child);
      graph.addVertex(child);
      vertices.put(child.getId(), child);
      graph.addEdge("c" + child.getId(), child, vertices.get(child.getParent()));
    }
    for (Subscription subscription : eventSubscriptions) {
      System.out.println("Subscription " + subscription);
      graph.addVertex(subscription);
      graph.addEdge("s" + subscription.getId(), subscription, vertices.get(subscription.getExecution()));
    }
    return graph;
  }

  private void renderGraph(Forest<Vertex, String> graph) {
    Layout<Vertex, String> layout = new TreeLayout<Vertex, String>(graph);
    VisualizationViewer<Vertex, String> vv = new VisualizationViewer<Vertex, String>(layout, new Dimension(350, 350));
    vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line());
    vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<Vertex>());
    vv.getRenderContext().setArrowFillPaintTransformer(new ConstantTransformer(Color.lightGray));
    JFrame frame = new JFrame("Simple Graph View");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getContentPane().add(vv);
    frame.pack();
    frame.setVisible(true);
  }

  private void closeConnection() {
    try {
      connection.close();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * connect to the local in-memory DB
   */
  private void connectLocalTcp() {
    try {
      connection = DriverManager.getConnection("jdbc:h2:tcp://localhost/mem:activiti", "sa", "");
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * fetch parent execution entities
   *
   * @return executions
   */
  private List<Execution> getParentExecutions() {
    try {
      Statement statement = connection.createStatement();
      ResultSet resultSet = statement.executeQuery("SELECT * FROM ACT_RU_EXECUTION WHERE PARENT_ID_ IS NULL");
      List<Execution> executions = new ArrayList<Execution>();
      while (resultSet.next()) {
        executions.add(new Execution(//
            resultSet.getString("ID_"), //
            null, //
            resultSet.getString("ACT_INST_ID_"), //
            resultSet.getBoolean("IS_ACTIVE_"), //
            resultSet.getBoolean("IS_CONCURRENT_"), //
            resultSet.getBoolean("IS_SCOPE_")
        ));
      }
      statement.close();
      return executions;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * fetch child execution entities
   *
   * @return executions
   */
  private List<Execution> getChildExecutions() {
    try {
      Statement statement = connection.createStatement();
      ResultSet resultSet = statement.executeQuery("SELECT * FROM ACT_RU_EXECUTION WHERE PARENT_ID_ IS NOT NULL");
      List<Execution> executions = new ArrayList<Execution>();
      while (resultSet.next()) {
        executions.add(new Execution(//
          resultSet.getString("ID_"), //
          resultSet.getString("PARENT_ID_"), //
          resultSet.getString("ACT_INST_ID_"), //
          resultSet.getBoolean("IS_ACTIVE_"), //
          resultSet.getBoolean("IS_CONCURRENT_"), //
          resultSet.getBoolean("IS_SCOPE_")
        ));
      }
      statement.close();
      return executions;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * fetch event subscriptions
   *
   * @return subscriptions
   */
  private List<Subscription> getEventSubscriptions() {
    try {
      Statement statement = connection.createStatement();
      ResultSet resultSet = statement.executeQuery("SELECT * FROM ACT_RU_EVENT_SUBSCR");
      List<Subscription> subscriptions = new ArrayList<Subscription>();
      while (resultSet.next()) {
        subscriptions.add(new Subscription(//
          resultSet.getString("ID_"),
          resultSet.getString("EXECUTION_ID_"),
          resultSet.getString("EVENT_TYPE_"),
          resultSet.getString("EVENT_NAME_")
        ));
      }
      statement.close();
      return subscriptions;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
