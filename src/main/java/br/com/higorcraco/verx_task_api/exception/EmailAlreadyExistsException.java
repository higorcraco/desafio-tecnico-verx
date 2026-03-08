package br.com.higorcraco.verx_task_api.exception;

public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super("Este email já está cadastrado: " + email);
    }
}
