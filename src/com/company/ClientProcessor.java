package com.company;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintStream;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;

import static com.company.Server.*;

public class ClientProcessor implements Runnable{

    private Socket sock;
    private BufferedInputStream reader = null;
    public String login;
    private PrintStream ecritureEcran;
    public ClientProcessor(Socket pSock){
        sock = pSock;
    }
    static public String toMessage;
    static public String toLogin;

    //Le traitement lancé dans un thread séparé
    public void run(){
        System.err.println("Traitement du client");
        boolean fisrtConnexion = true;
        boolean closeConnexion = false;

        while(!sock.isClosed()){

            try {
                ecritureEcran = new PrintStream(sock.getOutputStream(), false);
                reader = new BufferedInputStream(sock.getInputStream());

                if(fisrtConnexion) {
                    login = read();
                    logins.add(login);
                    send("Welcome " + login + "!");
                    fisrtConnexion = false;
                }

                String response = read();
                InetSocketAddress remote = (InetSocketAddress)sock.getRemoteSocketAddress();

                String console = "";
                console = "/" + remote.getAddress().getHostAddress() + " ("+ this.login + ")" +  ">" + response;
                System.out.println("\n" + console);

                if(response.equals("getUtilisateursOnline")) {
                    StringBuilder temp = new StringBuilder();
                    temp.append("Client connecté(s): ");
                    for (String s : logins) {
                        temp.append(s).append(" | ");
                    }
                    send(temp.toString());
                } if(response.length() > 8 && response.substring(0, 8).equals("speakTo/")) {
                    for (ClientProcessor client : clients) {
                        if (client.getLogin().equals(response.split("/")[1])) {
                            client.send(login + "> " + response.split("/")[2]);
                        }
                    }
                }
                if(response.length() > 3 && response.substring(0,9).equals("speakAll/")) {
                    for (ClientProcessor client : clients) {
                        client.send(login + " to all> " + response.split("/")[1]);
                    }
                }
                else {
                    send(response);
                }


                if(response.equals("quit".toLowerCase())){
                    send("Connexion closed");
                    System.err.println("Connexion closed ");
                    deconnexionLogin();
                    reader = null;
                    sock.close();
                    break;
                }
            }catch(SocketException e){
                deconnexionLogin();
                System.err.println("Connexion interrompu ! ");
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void deconnexionLogin() {
        for (int i = 0; i < logins.size(); i++) {
            if(logins.get(i).equals(login))
                logins.remove(i);
        }
    }

    /*private void speakTo(String login, String message) {
        if(login.equals(this.login)) {
            send(login + "> " +message);
        }
    }*/

    private void send(String message) {
        ecritureEcran.println(message);
        ecritureEcran.flush();
    }

    private String read() throws IOException{
        String response = "";
        InetSocketAddress remote = (InetSocketAddress)sock.getRemoteSocketAddress();
        String host = remote.getAddress().getHostAddress();

        Date date = new Date();
        int stream;
        byte[] b = new byte[4096];
        stream = reader.read(b);
        response = new String(b, 0, stream);
        Fichier file = new Fichier();
        file.ecrire(host, response, date, this.login);
        return response;
    }

    public String getLogin() {
        return this.login;
    }
}