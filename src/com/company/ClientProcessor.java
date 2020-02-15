package com.company;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintStream;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;

import static com.company.Server.logins;

public class ClientProcessor implements Runnable{

    private Socket sock;
    private BufferedInputStream reader = null;
    private String login;
    private PrintStream ecritureEcran;
    public ClientProcessor(Socket pSock){
        sock = pSock;
    }

    //Le traitement lancé dans un thread séparé
    public void run(){
        System.err.println("Lancement du traitement de la connexion cliente");
        boolean fisrtConnexion = true;
        boolean closeConnexion = false;

        while(!sock.isClosed()){

            try {
                ecritureEcran = new PrintStream(sock.getOutputStream(), false);
                reader = new BufferedInputStream(sock.getInputStream());

                if(fisrtConnexion) {
                    login = read();
                    logins.add(login);
                    ecritureEcran.println("Welcome " + login + "!");
                    ecritureEcran.flush();
                    fisrtConnexion = false;
                }

                String response = read();
                InetSocketAddress remote = (InetSocketAddress)sock.getRemoteSocketAddress();

                String debug = "";
                debug = "/" + remote.getAddress().getHostAddress() + " ("+ this.login + ")" +  ">" + response;
                System.out.println("\n" + debug);


                String toSend = "";
                if(response.equals("quit".toLowerCase()))
                    toSend = "Communication terminée";

                else if(response.equals("getUtilisateursOnline")) {
                    StringBuilder temp = new StringBuilder();
                    for (String s : logins) {
                        temp.append(s).append(" | ");
                    }
                    ecritureEcran.println(temp.toString());
                    ecritureEcran.flush();
                }
                else {
                    ecritureEcran.println(response);
                    ecritureEcran.flush();
                }


                if(response.equals("quit".toLowerCase())){
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
}