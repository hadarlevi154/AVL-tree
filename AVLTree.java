import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * AVLTree
 *
 * An implementation of a AVL Tree with
 * distinct integer keys and info
 *
 *
 * Hadar Levi, 209006360, hadarlevi2
 * Shani Noyman, 208660654, shaninoyman
 *
 *
 */

public class AVLTree {
    IAVLNode root;
    IAVLNode min, max;

    //all valid/ invalid cases of node's rank differences.
    final int[] valid11 = {1,1};
    final int[] valid21 = {2,1};
    final int[] valid12 = {1,2};
    final int[] invalid01 = {0,1};
    final int[] invalid10 = {1,0};
    final int[] invalid02 = {0,2};
    final int[] invalid20 = {2,0};
    final int[] invalid22 = {2,2};
    final int[] invalid31 = {3,1};
    final int[] invalid13 = {1,3};


    //constructor for tree with first node
    public AVLTree(int key, String info)
    {
        this.root = new AVLNode(key, info, AVLNode.virtual, AVLNode.virtual, null);
        this.min = this.root;
        this.max = this.root;
    }

    //costructor for empty tree
    public AVLTree()
    {
        this.root = null;
        this.min = null;
        this.max = null;
    }

    /**
     * public boolean empty()
     *
     * returns true if and only if the tree is empty
     *
     */
    public boolean empty() {
        return (this.root == null) || (this.root == AVLNode.virtual);
    }

    /**
     * public String search(int k)
     *
     * returns the info of an item with key k if it exists in the tree
     * otherwise, returns null
     */
    public String search(int k)
    {
        if (this.empty())
            return null;
        return searchRec(this.root, k);
    }

    //recursive func for searching node- as we learned in class
    private String searchRec(IAVLNode node, int k)
    {
        if (!node.isRealNode())
            return null;
        if (node.getKey() == k)
            return node.getValue();
        if (k < node.getKey())
            return searchRec(node.getLeft(), k);
        else
            return searchRec(node.getRight(), k);
    }


    /**
     * public int insert(int k, String i)
     *
     * inserts an item with key k and info i to the AVL tree.
     * the tree must remain valid (keep its invariants).
     * returns the number of rebalancing operations, or 0 if no rebalancing operations were necessary.
     * promotion/rotation - counted as one rebalnce operation, double-rotation is counted as 2.
     * returns -1 if an item with key k already exists in the tree.
     */
    public int insert(int k, String i)
    {
        //inserting for an empty tree- no need to rebalance, no rebalancing operations
        if (this.empty()) {
            this.root = new AVLNode(k, i, AVLNode.virtual, AVLNode.virtual, null);
            this.min = this.root;
            this.max = this.root;
            return 0;
        }
        else {
            IAVLNode insertAfter = TreePosition(this.root, k); //node which new node should be after
            if (insertAfter.getKey() == k) // if node already in tree
                return -1;

            //creating a node to insert the tree
            IAVLNode newNode = new AVLNode(k, i, AVLNode.virtual, AVLNode.virtual, insertAfter);

            //newNode needs to be right child
            if (k > insertAfter.getKey()) {
                insertAfter.setRight(newNode);

                //setting max to be newNode if necessary
                if (k > this.max.getKey())
                    this.max = newNode;
            }

            //newNode needs to be left child
            else {
                insertAfter.setLeft(newNode);

                //setting min to be newNode if necessary
                if (k < this.min.getKey())
                    this.min = newNode;
            }
            return rebalance(insertAfter);
        }
    }

    public int rebalance(IAVLNode node)
    {
        if (node == null)
            return 0;

        //initializing counter to count rebalancing operations
        int rebalanceCounter = 0;

        //setting node's variables
        node.calcAndSetCurrentDiff();
        node.calcAndSetSize();

        while ( node != null && !isValidDiff(node))
        {
            //case 0-1, promote
            if (Arrays.equals(node.getDiff(), invalid01))
            {
                promote(node);

                node.calcAndSetCurrentDiff();
                node.calcAndSetSize();

                rebalanceCounter++; //1 promote
            }
            //case 1-0, promote
            else if (Arrays.equals(node.getDiff(), invalid10))
            {
                promote(node);

                node.calcAndSetCurrentDiff();
                node.calcAndSetSize();

                rebalanceCounter++; //1 promote
            }
            //single rotation
            //case 0-2 with left 1-2
            else if (Arrays.equals(node.getDiff(), invalid02) && Arrays.equals(node.getLeft().getDiff(), valid12))
            {
                rightRotation(node);
                demote(node);

                node.calcAndSetCurrentDiff();
                node.calcAndSetSize();

                node.getParent().calcAndSetCurrentDiff();
                node.getParent().calcAndSetSize();

                rebalanceCounter += 2; //1 demote, 1 rotation

            }

            //case 2-0 with right 2-1
            else if (Arrays.equals(node.getDiff(), invalid20) && Arrays.equals(node.getRight().getDiff(), valid21))
            {
                leftRotation(node);
                demote(node);

                node.calcAndSetCurrentDiff();
                node.calcAndSetSize();

                node.getParent().calcAndSetCurrentDiff();
                node.getParent().calcAndSetSize();

                rebalanceCounter += 2; //1 demote, 1 rotation

            }

            //double rotation
            //case 0-2 with left child 2-1
            else if (Arrays.equals(node.getDiff(), invalid02) && Arrays.equals(node.getLeft().getDiff(), valid21))
            {
                leftRotation(node.getLeft());
                rightRotation(node);
                demote(node);
                node.getParent().getLeft().setHeight(node.getParent().getLeft().getHeight()-1);
                node.getParent().setHeight(node.getParent().getHeight()+1);

                node.calcAndSetCurrentDiff();
                node.calcAndSetSize();

                node.getParent().getLeft().calcAndSetCurrentDiff();
                node.getParent().getLeft().calcAndSetSize();

                node.getParent().calcAndSetCurrentDiff();
                node.getParent().calcAndSetSize();

                rebalanceCounter += 5; //2 demote, 1 promote, 2 rotation

            }

            //case 2-0 with right child 1-2
            else if (Arrays.equals(node.getDiff(), invalid20) && Arrays.equals(node.getRight().getDiff(), valid12))
            {
                rightRotation(node.getRight());
                leftRotation(node);
                demote(node); 
                node.getParent().getRight().setHeight(node.getParent().getRight().getHeight()-1); 
                node.getParent().setHeight(node.getParent().getHeight()+1); 

                node.calcAndSetCurrentDiff();
                node.calcAndSetSize();

                node.getParent().getRight().calcAndSetCurrentDiff();
                node.getParent().getRight().calcAndSetSize();

                node.getParent().calcAndSetCurrentDiff();
                node.getParent().calcAndSetSize();

                rebalanceCounter += 5; //2 demote, 1 promote, 2 rotation

            }

            //demote
            //case 2-2
            else if (Arrays.equals(node.getDiff(), invalid22))
            {
                demote(node);

                node.calcAndSetCurrentDiff();
                node.calcAndSetSize();

                rebalanceCounter += 1; // 1 demote
            }

            //single rotation
            //case 3-1 with right child 1-1
            else if (Arrays.equals(node.getDiff(), invalid31) && Arrays.equals((node.getRight().getDiff()), valid11))
            {
                leftRotation(node);
                demote(node);
                promote(node.getParent());

                node.calcAndSetCurrentDiff();
                node.calcAndSetSize();

                node.getParent().calcAndSetCurrentDiff();
                node.getParent().calcAndSetSize();

                rebalanceCounter += 3; // 1 rotation 1 demote 1 promote
            }

            //case 1-3 with left child 1-1
            else if (Arrays.equals(node.getDiff(), invalid13) && Arrays.equals((node.getLeft().getDiff()), valid11))
            {
                rightRotation(node);
                demote(node);
                promote(node.getParent());

                node.calcAndSetCurrentDiff();
                node.calcAndSetSize();

                node.getParent().calcAndSetCurrentDiff();
                node.getParent().calcAndSetSize();

                rebalanceCounter += 3; // 1 rotation 1 demote 1 promote
            }

            //case 3-1 with child 2-1
            else if (Arrays.equals(node.getDiff(), invalid31) && Arrays.equals((node.getRight().getDiff()), valid21))
            {
                leftRotation(node);
                demote(node);
                demote(node);

                node.calcAndSetCurrentDiff();
                node.calcAndSetSize();

                node.getParent().calcAndSetCurrentDiff();
                node.getParent().calcAndSetSize();

                rebalanceCounter += 3; // 1 rotation 2 demote
            }

            //case 1-3 with left child 1-2
            else if (Arrays.equals(node.getDiff(), invalid13) && Arrays.equals((node.getLeft().getDiff()), valid12))
            {
                rightRotation(node);
                demote(node);
                demote(node);

                node.calcAndSetCurrentDiff();
                node.calcAndSetSize();

                node.getParent().calcAndSetCurrentDiff();
                node.getParent().calcAndSetSize();

                rebalanceCounter += 3; // 1 rotation 2 demote
            }

            //double rotations
            // case 3-1 with child 12
            else if (Arrays.equals(node.getDiff(), invalid31) && Arrays.equals((node.getRight().getDiff()), valid12))
            {
                rightRotation(node.getRight());
                leftRotation(node);
                demote(node);
                demote(node);
                demote(node.getParent().getRight());
                promote(node.getParent());

                node.calcAndSetCurrentDiff();
                node.calcAndSetSize();

                node.getParent().getRight().calcAndSetCurrentDiff();
                node.getParent().getRight().calcAndSetSize();

                node.getParent().calcAndSetCurrentDiff();
                node.getParent().calcAndSetSize();

                rebalanceCounter += 6; // 2 rotation 3 demote 1 promote
            }

            // case 1-3 with child 2-1
            else if (Arrays.equals(node.getDiff(), invalid13) && Arrays.equals((node.getLeft().getDiff()), valid21))
            {
                leftRotation(node.getLeft());
                rightRotation(node);
                demote(node);
                demote(node);
                demote(node.getParent().getLeft());
                promote(node.getParent());

                node.calcAndSetCurrentDiff();
                node.calcAndSetSize();

                node.getParent().getLeft().calcAndSetCurrentDiff();
                node.getParent().getLeft().calcAndSetSize();

                node.getParent().calcAndSetCurrentDiff();
                node.getParent().calcAndSetSize();

                rebalanceCounter += 6; // 2 rotation 3 demote 1 promote
            }

            //case 0-2 with left child 1-1
            else if (Arrays.equals(node.getDiff(), invalid02) && Arrays.equals(node.getLeft().getDiff(), valid11))
            {
                rightRotation(node);
                promote(node.getParent());

                node.calcAndSetCurrentDiff();
                node.calcAndSetSize();

                node.getParent().calcAndSetCurrentDiff();
                node.getParent().calcAndSetSize();

                rebalanceCounter += 2; //1 promote, 1 rotation
            }

            //case 2-0 with right child 1-1
            else if (Arrays.equals(node.getDiff(), invalid20) && Arrays.equals(node.getRight().getDiff(), valid11))
            {
                leftRotation(node);
                promote(node.getParent());

                node.calcAndSetCurrentDiff();
                node.calcAndSetSize();

                node.getParent().calcAndSetCurrentDiff();
                node.getParent().calcAndSetSize();

                rebalanceCounter += 2; //1 promote, 1 rotation
            }


            node.calcAndSetSize();

            node = node.getParent();

            if (node == null) //rebalanced until root- no need to maintain size
                return rebalanceCounter;
            else {
                node.calcAndSetCurrentDiff();
            }

        }
        maintainSizeAfterRebalance(node);
        return rebalanceCounter;
    }

    //searcing node with key k. returns the node with the key or the node which has to be k's parent (if k is not in tree)
    public IAVLNode TreePosition(IAVLNode x, int k) //x = root at first
    {
        IAVLNode y = null;
        while (x.isRealNode())
        {
            y = x;
            if (k == x.getKey())
                return x;
            else if (k < x.getKey())
                x = x.getLeft();
            else
                x = x.getRight();
        }
        return y;
    }

    //promoting a node- setting it's height up by one
    public void promote (IAVLNode node)
    {
        node.setHeight(node.getHeight()+1);
    }

    //demoting a node- setting its height down by one
    public void demote (IAVLNode node)
    {
        node.setHeight(node.getHeight()-1);
    }

    //returns true if node's rank differences are valid
    public boolean isValidDiff(IAVLNode node)
    {
        if (!node.getDiff().equals(valid11) || !node.getDiff().equals(valid21) || !node.getDiff().equals(valid12))
            return false;
        return true;
    }

    //making right rotation on node
    public void rightRotation(IAVLNode node)
    {
        IAVLNode originalNodeParent = node.getParent();
        IAVLNode originalLeftChild = node.getLeft();
        IAVLNode originalLeftChildRightChild = node.getLeft().getRight();

        originalLeftChild.setParent(originalNodeParent);
        originalLeftChild.setRight(node);
        node.setParent(originalLeftChild);
        node.setLeft(originalLeftChildRightChild);

        if (originalLeftChildRightChild.isRealNode())
            originalLeftChildRightChild.setParent(node);

        if (originalNodeParent != null) //changes original parent's child if it wasn't the root
        {
            if (isLeftChild(originalLeftChild))
                originalNodeParent.setLeft(originalLeftChild);
            if (!isLeftChild(originalLeftChild))
                originalNodeParent.setRight(originalLeftChild);
        }
        else //if rotate on root
            this.root = originalLeftChild;
    }

    //making left rotation on node
    public void leftRotation(IAVLNode node)
    {
        IAVLNode originalNodeParent = node.getParent();
        IAVLNode originalRightChild = node.getRight();
        IAVLNode originalRightChildLeftChild = node.getRight().getLeft();

        originalRightChild.setParent(originalNodeParent);
        originalRightChild.setLeft(node);
        node.setParent(originalRightChild);
        node.setRight(originalRightChildLeftChild);
        if (originalRightChildLeftChild != AVLNode.virtual)
            originalRightChildLeftChild.setParent(node);
        if (originalNodeParent != null) //changes original parent's child if it wasn't the root
        {
            if (!isLeftChild(originalRightChild))
                originalNodeParent.setRight(originalRightChild);
            if (isLeftChild(originalRightChild))
                originalNodeParent.setLeft(originalRightChild);
        }
        else //if rotate on root
            this.root = originalRightChild;
    }

    //returns true if node is left child
    public boolean isLeftChild(IAVLNode node)
    {
        return node.getParent().getKey() > node.getKey();
    }

    /**
     * public int delete(int k)
     *
     * deletes an item with key k from the binary tree, if it is there;
     * the tree must remain valid (keep its invariants).
     * returns the number of rebalancing operations, or 0 if no rebalancing operations were needed.
     * demotion/rotation - counted as one rebalnce operation, double-rotation is counted as 2.
     * returns -1 if an item with key k was not found in the tree.
     */
    public int delete(int k) //deletes the node and send to rebalance
    {
        int rebalanceCounter = -1;
        if (this.empty())
            return rebalanceCounter;
        IAVLNode nodeToDelete = TreePosition(this.root, k); //return the node if if exists, or its predecessor
        if (nodeToDelete.getKey() != k) //k is not in the tree
            return rebalanceCounter;

        //will rebalance after deletion- deleteLeaf/ deleteUnary/ deleteBinary
        IAVLNode parent = nodeToDelete.getParent();
        if (!nodeToDelete.getLeft().isRealNode() && !nodeToDelete.getRight().isRealNode()) // if nodeToDelete is leaf
            rebalanceCounter = rebalance(deleteLeaf(nodeToDelete, parent));
        else if (!nodeToDelete.getLeft().isRealNode() || !nodeToDelete.getRight().isRealNode()) //if nodeToDelete is unary
            rebalanceCounter = rebalance(deleteUnary(nodeToDelete, parent));
        else //if nodeToDelete is binary
            rebalanceCounter = rebalance(deleteBinary(nodeToDelete, parent));

        if (empty()) //the only node in tree deleted
        {
            this.min = null;
            this.max = null;
            return rebalanceCounter;
        }

        //if k was min/ max- finding the new min/max
        if (k == this.max.getKey())
            this.max = findMax(this.root);
        else if (k == this.min.getKey())
            this.min = findMin(this.root);

        return rebalanceCounter;

    }

    //deletes nodeToDelete and returns its parent for rebalance.
    public IAVLNode deleteLeaf (IAVLNode nodeToDelete, IAVLNode parent)
    {
    	//nodeToDelete is a leaf with no parent- only node in the tree. tree is empty after deletion
        if (parent == null) 
        {
            this.root = null;
            this.min = null;
            this.max = null;
            nodeToDelete.setLeft(null);
            nodeToDelete.setRight(null);
            return null;
        }
        else //the tree won't be empty after deletion
        {
            if (isLeftChild(nodeToDelete)) //leaf is a left child
                parent.setLeft(AVLNode.virtual);
            else //leaf is a right child
                parent.setRight(AVLNode.virtual);
            nodeToDelete.setParent(null);
            return parent;
        }
    }

    //deletes nodeToDelete and returns its parent
    public IAVLNode deleteUnary (IAVLNode nodeToDelete, IAVLNode parent)
    {
        if (parent == null) // nodeToDelete is root
        {
            if (nodeToDelete.getLeft().isRealNode()) //nodeToDelete has only left child
            {
                this.root = nodeToDelete.getLeft();
                nodeToDelete.getLeft().setParent(null);
            }
            else //nodeToDelete has only right child
            {
                this.root = nodeToDelete.getRight();
                nodeToDelete.getRight().setParent(null);
            }
            nodeToDelete.setRight(null);
            nodeToDelete.setLeft(null);
            return null;
        }
        else // nodeToDelete is not root
        {
            if (!nodeToDelete.getLeft().isRealNode()) //nodeToDelete has only right child
            {
                if (isLeftChild(nodeToDelete)) //nodeToDelete is left child
                    parent.setLeft(nodeToDelete.getRight()); //skip nodeToDelete
                else //nodeToDelete is right child
                    parent.setRight(nodeToDelete.getRight()); //skip nodeToDelete

                nodeToDelete.getRight().setParent(parent);
                nodeToDelete.setParent(null);
                nodeToDelete.setRight(null);
            }
            else if (!nodeToDelete.getRight().isRealNode()) //nodeToDelete has only right child
            {
                if (isLeftChild(nodeToDelete)) //nodeToDelete is left child
                    parent.setLeft(nodeToDelete.getLeft()); //skip nodeToDelete
                else //nodeToDelete is right child
                    parent.setRight(nodeToDelete.getLeft()); //skip nodeToDelete

                nodeToDelete.getLeft().setParent(parent);
                nodeToDelete.setParent(null);
                nodeToDelete.setLeft(null);
            }
            return parent;
        }
    }

    //deletes nodeToDelete and returns its parent
    public IAVLNode deleteBinary(IAVLNode nodeToDelete, IAVLNode parent)
    {
        IAVLNode suc = successor(nodeToDelete); //finding the successor for nodeToDelete

        boolean wasNodeToDeleteChild = false;
        IAVLNode nodeOfSuc;
        //new node for suc - to replace with nodeToDelete
        if (suc.getParent().getKey() == nodeToDelete.getKey())  //nodeToDelete's successor is its child- suc is right child for sure
        {
            nodeOfSuc = new AVLNode(suc.getKey(), suc.getValue(), nodeToDelete.getLeft(), suc.getRight(), parent);
            wasNodeToDeleteChild = true;
        }
        else //nodeToDelete's successor is not its child
            nodeOfSuc = new AVLNode(suc.getKey(), suc.getValue(), nodeToDelete.getLeft(), nodeToDelete.getRight(), parent);

        //setting nodeOfSuc's height
        nodeOfSuc.setHeight(Math.max(nodeOfSuc.getLeft().getHeight(), nodeOfSuc.getRight().getHeight())+1);

        IAVLNode sucParent = suc.getParent();

        //successor has no left child for sure.
        if (!suc.getLeft().isRealNode() && !suc.getRight().isRealNode()) //suc is a leaf
            deleteLeaf(suc, sucParent);
        else //suc is unary
            deleteUnary(suc, sucParent);

        nodeToDelete.setParent(null);
        nodeToDelete.setLeft(null);
        nodeToDelete.setRight(null);

        if (nodeOfSuc.getRight().isRealNode()) //nodeOfSuc's right child is not virtual
            nodeOfSuc.getRight().setParent(nodeOfSuc);
        if (nodeOfSuc.getLeft().isRealNode()) //nodeOfSuc's left child is not virtual
            nodeOfSuc.getLeft().setParent(nodeOfSuc);

        if (nodeOfSuc.getParent() != null) //nodeOfSuc is not root
        {
            if (isLeftChild(nodeOfSuc)) //nodeOfSuc is left child
                nodeOfSuc.getParent().setLeft(nodeOfSuc);
            else //nodeOfSuc is right child
                nodeOfSuc.getParent().setRight(nodeOfSuc);
        }
        else //nodeOfSuc is root
            this.root = nodeOfSuc;


        if (wasNodeToDeleteChild) //suc's parent was nodeToDelete itself (always rebalancing from suc's parent)
            return nodeOfSuc;
        //rebalance from suc's parent because its the first node that rank diff may changed
        return sucParent;
    }

    //returns node's successor
    public IAVLNode successor(IAVLNode node)
    {
        if (this.max.getKey() == node.getKey())
            return null;
        if (node.getRight().isRealNode())
            return findMin(node.getRight());

        IAVLNode parent = node.getParent();
        while (parent != null && node == parent.getRight())
        {
            node = node.getParent();
            parent = node.getParent();
        }
        return parent;
    }

    //calculating and setting all nodes sizes from node until root
    public void maintainSizeAfterRebalance(IAVLNode node)
    {
        while (node != null)
        {
            node.calcAndSetSize();
            node = node.getParent();
        }
    }

    /**
     * public String min()
     *
     * Returns the info of the item with the smallest key in the tree,
     * or null if the tree is empty
     */
    public String min()
    {
        if (this.empty()) //tree is empty
            return null;
        return this.min.getValue();
    }

    /**
     * public String max()
     *
     * Returns the info of the item with the largest key in the tree,
     * or null if the tree is empty
     */
    public String max()
    {
        if (this.empty()) //tree is empty
            return null;
        return this.max.getValue();
    }


    
    /**
     * public int[] keysToArray()
     *
     * Returns a sorted array which contains all keys in the tree,
     * or an empty array if the tree is empty.
     */
    public int[] keysToArray()
    {
        List<IAVLNode> nodeList = inOrder(this.root); 
        int[] keys = new int[this.size()];
        int i=0;
        for (IAVLNode node : nodeList)
        {
            keys[i] = node.getKey(); //inserting node's keys to array
            i++;
        }
        return keys;
    }
    
    //returns an IAVLNode list which contains all nodes sorted by their keys   
    public List<IAVLNode> inOrder(IAVLNode node)
    {
        if (node == null || !node.isRealNode())
            return new ArrayList<>();
        List<IAVLNode> list = new LinkedList<>();
        list.addAll(inOrder(node.getLeft()));
        list.add(node);
        list.addAll(inOrder(node.getRight()));
        return list;
    }

    /**
     * public String[] infoToArray()
     *
     * Returns an array which contains all info in the tree,
     * sorted by their respective keys,
     * or an empty array if the tree is empty.
     */
    public String[] infoToArray()
    {
        List<IAVLNode> nodeList = inOrder(this.root);
        String[] infos = new String[this.size()];
        int i=0;
        for (IAVLNode node : nodeList)
        {
            infos[i] = node.getValue(); //inserting node's values to array
            i++;
        }
        return infos;
    }

    /**
     * public int size()
     *
     * Returns the number of nodes in the tree.
     *
     * precondition: none
     * postcondition: none
     */
    public int size()
    {
        if (this.empty()) //tree is empty
            return 0;
        return this.root.getSize();
    }

    /**
     * public int getRoot()
     *
     * Returns the root AVL node, or null if the tree is empty
     *
     * precondition: none
     * postcondition: none
     */
    public IAVLNode getRoot()
    {
        return this.root;
    }

    /**
     * public string split(int x)
     *
     * splits the tree into 2 trees according to the key x.
     * Returns an array [t1, t2] with two AVL trees. keys(t1) < x < keys(t2).
     * precondition: search(x) != null (i.e. you can also assume that the tree is not empty)
     * postcondition: none
     */

    public AVLTree[] split(int x)
    {
        IAVLNode node = TreePosition(this.root, x);
        
        AVLTree tSmall = tmpTree(node.getLeft()); //tSmall will contain all nodes with smaller keys. at first- contains node's left subtree 
        AVLTree tBig = tmpTree(node.getRight()); //tBig will contain all nodes with larger keys. at first- contains node's right subtree 

        AVLTree tmpTree;
        IAVLNode tmpNode;
        IAVLNode parent = node.getParent();

        //going from node up until root
        while (parent != null)
        {
            
            if (!isLeftChild(node)) //join tSmall with tmpTree (subtree of node, included) , join on parent
            {
                tmpTree = tmpTree(parent.getLeft()); //creating a temporary tree to join with tSmall
                tmpNode = new AVLNode(parent.getKey(), parent.getValue(), AVLNode.virtual, AVLNode.virtual, null); //creating a temporary node to join both trees on

                tSmall.join(tmpNode, tmpTree);
            }
            else //join tBig with tmpTree (subtree of node, included) , join on parent
            {

                tmpTree = tmpTree(parent.getRight());  //creating a temporary tree to join with tBig
                tmpNode = new AVLNode(parent.getKey(), parent.getValue(), AVLNode.virtual, AVLNode.virtual, null); //creating a temporary node to join both trees on
                tBig.join(tmpNode, tmpTree);
            }
            node = parent;
            parent = node.getParent();
        }

        //setting both tree's min, max
        tSmall.min = findMin(tSmall.getRoot());
        tSmall.max = findMax(tSmall.getRoot());
        tBig.min = findMin(tBig.getRoot());
        tBig.max = findMax(tBig.getRoot());
        return new AVLTree[] {tSmall, tBig};

    }

    /**
     * public join(IAVLNode x, AVLTree t)
     *
     * joins t and x with the tree.
     * Returns the complexity of the operation (|tree.rank - t.rank| + 1).
     * precondition: keys(x,t) < keys() or keys(x,t) > keys(). t/tree might be empty (rank = -1).
     * postcondition: none
     */
    public int join(IAVLNode x, AVLTree t)
    {
        //joinCost is the complexity of the operation- returned value
        int joinCost = Math.abs(this.getTreeRank() - t.getTreeRank()) + 1;

        //both trees are empty- same as insert x to this
        if (this.empty() && t.empty())
        {
            insert(x.getKey(), x.getValue());

            //calculating all of x's variables
            calcAndSetHeight(this.root);
            this.root.calcAndSetCurrentDiff();
            this.root.calcAndSetSize();

            return joinCost;
        }

        //only one tree is empty- join x to this (if this was empty before- turning this to be t) 
        else if (t.empty() || this.empty())
        {
            if (this.empty())
            {
                this.root = t.getRoot();
                this.max = t.max;
                this.min = t.min;
            }

            //x will be this.min/max (because of the precondition). Will only add it to the tree and rebalance 
            //x is smaller than all of this's nodes
            if (x.getKey() < this.root.getKey())
            {
                this.min = findMin(this.root);
                IAVLNode node = this.min;

                x.setRight(node);

                if (x.getRight().getParent() != null) //x won't be root after join
                {
                    x.getRight().getParent().setLeft(x);
                    x.setParent(x.getRight().getParent());
                }

                else if (node == this.root) //node was root and now it is x's child- x is root
                    this.root = x;

                x.getRight().setParent(x);

                //calculating all of x's variables before rebalancing
                calcAndSetHeight(x);
                x.calcAndSetCurrentDiff();
                x.calcAndSetSize();

                rebalance(x);

            }

            //x is bigger than all of this's nodes
            else if (x.getKey() > this.root.getKey())
            {
                this.max = findMax(this.root);
                IAVLNode node = this.max;
                x.setLeft(node);

                if (x.getLeft().getParent() != null) //x won't be root after join
                {
                    x.getLeft().getParent().setRight(x);
                    x.setParent(x.getLeft().getParent());
                }

                else if (node == this.root) //node was root and now it is x's child- x is root
                    this.root = x;

                x.getLeft().setParent(x);

                //calculating all of x's variables before rebalancing
                calcAndSetHeight(x);
                x.calcAndSetCurrentDiff();
                x.calcAndSetSize();

                rebalance(x);
            }

            return joinCost;
        }

        //both trees are not empty
        AVLTree tRight, tLeft;

        //making new pointers to the tree that will be on the left and the tree that will be on the right
        //this- the tree that will contain both of them after join- will have a new min/ max.
        if (this.isBigger(t, x))
        {
            tRight = this;
            tLeft = t;
            this.min = findMin(t.getRoot()); //setting this new min
            this.max = findMax(this.root);
        }
        else
        {
            tRight = t;
            tLeft = this;
            this.max = findMax(t.getRoot()); //setting this new max
            this.min = findMin(this.root);
        }

        //both trees has he same rank- need to set x as this.root, set pointer for x as the root (from and to x), and set his variables
        //no need to rebalance
        if (tRight.getTreeRank() == tLeft.getTreeRank())
        {
            x.setLeft(tLeft.getRoot());
            x.setRight(tRight.getRoot());

            x.getLeft().setParent(x);
            x.getRight().setParent(x);

            //setting x's variables
            calcAndSetHeight(x);
            x.calcAndSetCurrentDiff();
            x.calcAndSetSize();

            this.root = x;
        }

        //right tree's rank is bigger than left tree's 
        //will go down-left from tRight.root until getting to a node with tLeft's rank/ (tLeft's rank - 1)
        else if (tRight.getTreeRank() > tLeft.getTreeRank())
        {
            IAVLNode node = tRight.getRoot();
            
            IAVLNode parent = AVLNode.virtual; //in case first vertex on the left with rank <= k is a virtual leaf.  
            
            while (node.getHeight() > tLeft.getTreeRank())
            {
                parent = node;
                node = node.getLeft();
            }
            
            x.setLeft(tLeft.getRoot());
            x.setRight(node);

            parent.setLeft(x);
            x.setParent(parent);

            x.getLeft().setParent(x);
            x.getRight().setParent(x);

            //setting x's variables before rebalancing 
            calcAndSetHeight(x);
            x.calcAndSetCurrentDiff();
            x.calcAndSetSize();

            this.root = tRight.getRoot(); //this root will be the original root of the higher tree (bigger rank) 

            rebalance(x);
        }

        else if (tRight.getTreeRank() < tLeft.getTreeRank())
        {
            IAVLNode node = tLeft.getRoot();
            IAVLNode parent = AVLNode.virtual; //in case first vertex on the right with rank <= k is a virtual leaf
            while (node.getHeight() > tRight.getTreeRank())
            {
                parent = node;
                node = node.getRight();
            }
            x.setLeft(node);
            x.setRight(tRight.getRoot());

            parent.setRight(x);
            x.setParent(parent);

            x.getRight().setParent(x);
            x.getLeft().setParent(x);

            //setting x's variables before rebalancing
            calcAndSetHeight(x);
            x.calcAndSetCurrentDiff();
            x.calcAndSetSize();

            this.root = tLeft.getRoot(); //this root will be the original root of the higher tree (bigger rank) 
            rebalance(x);
        }


        return joinCost;
    }

    
    //creating a temporary tree from a sub tree of this. used only in split. disconnecting subtree from its original tree 
    public AVLTree tmpTree (IAVLNode rootNode)
    {
        AVLTree tmpTree = new AVLTree();
        if (!rootNode.isRealNode()) { //trying to create a subtree from a virtual leaf. returning an empty tree.
            return tmpTree;
        }
        rootNode.setParent(null); //diconnecting rootNode from its original parent.
        tmpTree.root = rootNode;

        //setting root's values
        calcAndSetHeight(tmpTree.getRoot());
        tmpTree.getRoot().calcAndSetCurrentDiff();
        tmpTree.getRoot().calcAndSetSize();
        
        return tmpTree;

    }
    
    
    //returns IAVLNode minimum node of the tree
    public IAVLNode findMin(IAVLNode node)
    {
        if (node == null)
            return null;
        IAVLNode min = node;
        while (node.getLeft().isRealNode())
        {
            node = node.getLeft();
            min = node;
        }
        return min;
    }

    
    //returns IAVLNode maximum node of the tree
    public IAVLNode findMax(IAVLNode node)
    {
        if (node == null)
            return null;
        IAVLNode max = node;
        while (node.getRight().isRealNode())
        {
            node = node.getRight();
            max = node;
        }
        return max;
    }
    
    //setting node's height from its children
    public void calcAndSetHeight(IAVLNode node)
    {
        node.setHeight(Math.max(node.getLeft().getHeight(), node.getRight().getHeight()) + 1);
    }

    //returns true if x's key is smaller than all this's nodes keys (used only in join, based on its precondition).
    public boolean isBigger(AVLTree t, IAVLNode x)
    {
        return (this.root.getKey() > x.getKey());
    }

    //Returns tree's rank
    public int getTreeRank()
    {
        if (this.empty()) //tree is empty
            return -1;
        return this.root.getHeight();
    }


    /**
     * public interface IAVLNode
     * ! Do not delete or modify this - otherwise all tests will fail !
     */
    public interface IAVLNode{
        public int getKey(); //returns node's key (for virtuval node return -1)
        public String getValue(); //returns node's value [info] (for virtuval node return null)
        public void setLeft(IAVLNode node); //sets left child
        public IAVLNode getLeft(); //returns left child (if there is no left child return null)
        public void setRight(IAVLNode node); //sets right child
        public IAVLNode getRight(); //returns right child (if there is no right child return null)
        public void setParent(IAVLNode node); //sets parent
        public IAVLNode getParent(); //returns the parent (if there is no parent return null)
        public boolean isRealNode(); // Returns True if this is a non-virtual AVL node
        public void setHeight(int height); // sets the height of the node
        public int getHeight(); // Returns the height of the node (-1 for virtual nodes)
        public int[] getDiff(); // Returns the rank differences from the node's children
        public void calcAndSetCurrentDiff(); //sets node's rank differences
        public int getSize(); //Returns node's size (number of real nodes under node includes
        public void calcAndSetSize(); //sets node's size
    }

    /**
     * public class AVLNode
     *
     * If you wish to implement classes other than AVLTree
     * (for example AVLNode), do it in this file, not in
     * another file.
     * This class can and must be modified.
     * (It must implement IAVLNode)
     */
    public static class AVLNode implements IAVLNode{
        private int key, height;
        private String info;
        private IAVLNode left, right, parent;
        private int[] diff;
        private int size;
        public static IAVLNode virtual =  new AVLNode();

        //constructor for a regular node
        public AVLNode(int key, String info, IAVLNode left, IAVLNode right, IAVLNode parent)
        {
            this.key = key;
            this.info = info;
            this.left = left;
            this.right = right;
            this.parent = parent;
            this.height = 0;
            this.diff = new int[] {1,1}; //rank differences from it's children
            this.size = this.left.getSize() + this.right.getSize() + 1; //size of node's subtree (itself includes)
        }

        //constructor for virtual node- used only once- static virtual for all AVLNode class.
        public AVLNode()
        {
            this.key = -1;
            this.info = null;
            this.left = null;
            this.right = null;
            this.parent = null;
            this.height = -1;
            this.diff = null;
            this.size = 0;
        }

        public int getKey()
        {
            return this.key;
        }
        public String getValue()
        {
            return this.info;
        }
        public void setLeft(IAVLNode node)
        {
            this.left = node;
        }
        public IAVLNode getLeft()
        {
            return this.left;
        }
        public void setRight(IAVLNode node)
        {
            this.right = node;
        }
        public IAVLNode getRight()
        {
            return this.right;
        }
        public void setParent(IAVLNode node)
        {
            this.parent = node;
        }
        public IAVLNode getParent()
        {
            return this.parent;
        }

        // Returns True if this is a non-virtual AVL node
        public boolean isRealNode()
        {
            return (this.key != -1) ;
        }
        public void setHeight(int height)
        {
            this.height = height;
        }
        public int getHeight()
        {
            return this.height;
        }
        public void setDiff(int[] newDiff)
        {
            this.diff = newDiff;
        }
        public int[] getDiff()
        {
            return this.diff;
        }

        //calculating and setting this's current rank differences using its children ranks
        public void calcAndSetCurrentDiff()
        {
            int[] currentDiff = new int[2];
            currentDiff[0] = this.getHeight()-this.getLeft().getHeight();
            currentDiff[1] = this.getHeight()-this.getRight().getHeight();
            this.setDiff(currentDiff);
        }

        public int getSize()
        {
            return this.size;
        }

        //calculating and setting node's size from its children's sizes
        public void calcAndSetSize()
        {
            this.size = this.left.getSize() + this.right.getSize() + 1;
        }
    }
}


