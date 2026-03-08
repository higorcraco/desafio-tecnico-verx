package br.com.higorcraco.verx_task_api.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " com id=" + id + " não encontrado.");
    }
}
