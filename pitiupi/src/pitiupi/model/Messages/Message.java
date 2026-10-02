/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pitiupi.model.Messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Classe base para as mensagens trocadas entre a aplicação e os plugins.
 *
 * <p>As mensagens devem estender esta classe para utilizar o sistema de
 * comunicação da aplicação. Subclasses podem adicionar os dados e a lógica
 * necessários para representar diferentes tipos de mensagens.</p>
 *
 * <p>Por implementar {@link Serializable}, objetos derivados desta classe
 * podem ser convertidos em bytes para transmissão pela rede.</p>
 *
 * @author flavio
 */
public abstract class Message implements Serializable{

    /**
     * Serializa a mensagem para um vetor de bytes.
     *
     * @return representação serializada da mensagem.
     * @throws IOException caso ocorra um erro durante a serialização.
     */
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(baos);
        out.writeObject(this);
        out.flush();
        return baos.toByteArray();
    }
}
