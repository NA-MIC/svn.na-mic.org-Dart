package dart.server.wrapimpl;

import dart.server.wrap.*;
import freemarker.template.*;

import org.apache.log4j.Logger;

/**
 * Provides a FreeMarker node type wrapper around a test to allow
 * FreeMarker to walk test hierarchies. It also appropriately wraps
 * the test, so that FreeMarker can treat this as a node (for walking)
 * and as a test object (for calling, e.g., getResult).
 * @author Amitha Perera
 * @version $Revision:$
 */
public class TestTreeNode implements TemplateNodeModel, TemplateHashModel {
  static Logger logger = Logger.getLogger ( TestTreeNode.class );   

  TestTreeNode parent = null;

  /** Cached list of children. Will be generated on demand. */
  SimpleSequence children = null;

  /** The TestImpl object that we are wrapping */
  TestEntity data = null;

  String nodeType = null;

  /** Depth of this node in the tree. The root is at depth 0. */
  int depth = 0;

  /** Internal constructor used to create the child nodes on demand. */
  private TestTreeNode( TestEntity d, TestTreeNode p ) {
    data = d;
    parent = p;
    nodeType = p.nodeType;
    depth = p.depth + 1;
    logger.debug( "Creating child " + data.getQualifiedName() + " with parent " + p.data.getQualifiedName() );
  }

  /**
   * Create a node representation of the test d. The FreeMarker node
   * type of each node in this tree will be "type". This means that
   * <#visit> will call the macro @type to process this node.
   */
  public TestTreeNode( TestEntity d, String type ) {
    data = d;
    nodeType = type;
    logger.debug( "Creating root with " + data.getQualifiedName() );
  }

  public TestEntity getData() { return data; }

  public TemplateSequenceModel getChildNodes() {
    logger.debug( data.getQualifiedName() + ": getting children" );
    if ( children == null ) {
      TestList cren = data.selectChildren();
      children = new SimpleSequence( cren.size() );
      TestIterator it = cren.iterator();
      while ( it.hasNext() ) {
        TestEntity child = it.next();
        children.add( new TestTreeNode( child, this ) );
      }
    }
    logger.debug( "Got " + children.size() + " children." );
    return children;      
  }

  public TemplateNodeModel getParentNode() { return parent; }

  public String getNodeName() { return data.getName(); }

  public String getNodeNamespace() { return null; }

  public String getNodeType() { return nodeType; }

  public TemplateModel get( String key ) throws TemplateModelException {
    if( key.equals( "depth" ) ) {
      return new SimpleNumber(depth);
    } else {
      TemplateHashModel hash = (TemplateHashModel)ObjectWrapper.DEFAULT_WRAPPER.wrap(data);
      return hash.get(key);
    }
  }

  public boolean isEmpty() { return false; }
}
