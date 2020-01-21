package com.ucar.datalink.biz.job;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yang.wang09 on 2019-01-09 15:05.
 */
public class MyTest {


    public static void main(String[] args) {

        go();

    }


    static class Node {
        int val;
        Node next;

        @Override
        public String toString() {
            return "Node_" + val;
        }
    }

    public static Node create(int val) {
        Node n = new Node();
        n.val = val;
        return n;
    }

    public static void assemble(List<Node> list) {
        if(list==null || list.size()==0) {
            return;
        }
        Node head = new Node();
        for(Node n : list) {
            head.next = n;
            head = n;
        }
    }

    public static void printNode(Node n) {
        Node head = n;
        while(head != null) {
            System.out.print(head.val+"  ");
            head = head.next;
        }
        System.out.println();
    }

    public static void go() {
        Node n1 = create(1);
        Node n2 = create(2);
        Node n3 = create(3);
        Node n4 = create(4);
        Node n5 = create(5);
        Node n6 = create(6);
        Node n7 = create(7);

        List<Node> list = new ArrayList<>();
        list.add(n1);
        list.add(n2);
        list.add(n3);
        list.add(n4);
        list.add(n5);
        list.add(n6);
        list.add(n7);
        assemble(list);
        printNode(n1);

        Node head = n1;
        Node newHead = reverse(n1);
        printNode(newHead);

        Node x = new Node();
        x.val = 9;
        Node y = new Node();
        y.val = 10;
        x.next = y;
        Node a = reverse(x);
        printNode(a);

    }

    public static Node reverse(Node head) {
        Node pre = null;
        Node current = head;
        while(current != null) {
            Node tmp = current.next;
            current.next = pre;
            pre = current;
            current = tmp;
        }
        return pre;
    }


    public static Node reverse_4(Node head) {
        Node pre = null;
        Node current = head;
        while(current != null) {
            Node tmp = current.next;
            current.next = pre;
            pre = current;
            current = tmp;
        }
        return pre;
    }



    public static Node reverse_3(Node head) {
        Node pre = null;
        Node cur = head;
        while(cur != null) {
            Node tmp = cur.next;
            cur.next = pre;
            pre = cur;
            cur = tmp;
        }
        return pre;
    }


    public static Node reverse_2(Node head) {
        Node pre = null;
        Node cur = head;
        while(cur != null) {
            Node tmp = cur.next;
            cur.next = pre;
            pre = cur;
            cur = tmp;
        }
        return pre;
    }

}
