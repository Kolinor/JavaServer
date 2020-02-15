package com.company;

public class Main {

    public static void main(String[] args) {
	// write your code here
        Server t = new Server(5);
//        t.connexion(6001, "192.168.1.131");
        t.connexion(6002, "192.168.1.24");
        t.run();

    }
}
