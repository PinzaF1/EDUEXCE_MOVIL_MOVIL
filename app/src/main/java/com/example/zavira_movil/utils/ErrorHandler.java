package com.example.zavira_movil.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.example.zavira_movil.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import retrofit2.Response;

/**
 * Clase centralizada para el manejo consistente de errores en toda la aplicación.
 * Proporciona mensajes claros y contextuales según el tipo de error.
 */
public class ErrorHandler {

    private static final String TAG = "ErrorHandler";

    /**
     * Tipos de errores que puede manejar el sistema
     */
    public enum ErrorType {
        NETWORK_ERROR,          // Sin conexión a internet
        TIMEOUT_ERROR,          // Tiempo de espera agotado
        SESSION_EXPIRED,        // Sesión expirada (401)
        FORBIDDEN,              // Acceso denegado (403)
        NOT_FOUND,              // Recurso no encontrado (404)
        SERVER_ERROR,           // Error del servidor (500+)
        BAD_REQUEST,            // Solicitud incorrecta (400)
        UNKNOWN_ERROR           // Error desconocido
    }

    /**
     * Información detallada del error
     */
    public static class ErrorInfo {
        public ErrorType type;
        public String title;
        public String message;
        public String technicalDetails;
        public boolean canRetry;
        public int httpCode;

        public ErrorInfo(ErrorType type, String title, String message, String technicalDetails, boolean canRetry, int httpCode) {
            this.type = type;
            this.title = title;
            this.message = message;
            this.technicalDetails = technicalDetails;
            this.canRetry = canRetry;
            this.httpCode = httpCode;
        }
    }

    /**
     * Analiza una respuesta HTTP fallida y devuelve información del error
     */
    public static ErrorInfo analyzeHttpError(Response<?> response) {
        int code = response.code();
        String technicalDetails = "HTTP " + code;

        // Intentar leer el cuerpo del error
        String errorBody = null;
        try {
            if (response.errorBody() != null) {
                errorBody = response.errorBody().string();
                technicalDetails += ": " + errorBody;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error al leer errorBody", e);
        }

        // Clasificar por código HTTP
        switch (code) {
            case 400:
                return new ErrorInfo(
                    ErrorType.BAD_REQUEST,
                    "Solicitud Incorrecta",
                    "Los datos enviados no son válidos. Por favor, verifica la información e intenta nuevamente.",
                    technicalDetails,
                    true,
                    code
                );

            case 401:
                return new ErrorInfo(
                    ErrorType.SESSION_EXPIRED,
                    "Sesión Expirada",
                    "Tu sesión ha expirado por seguridad. Por favor, inicia sesión nuevamente.",
                    technicalDetails,
                    false,
                    code
                );

            case 403:
                return new ErrorInfo(
                    ErrorType.FORBIDDEN,
                    "Acceso Denegado",
                    "No tienes permisos para acceder a este contenido. Contacta con soporte si crees que es un error.",
                    technicalDetails,
                    false,
                    code
                );

            case 404:
                return new ErrorInfo(
                    ErrorType.NOT_FOUND,
                    "Recurso No Encontrado",
                    "El contenido que buscas no está disponible o fue eliminado.",
                    technicalDetails,
                    true,
                    code
                );

            case 500:
            case 502:
            case 503:
            case 504:
                return new ErrorInfo(
                    ErrorType.SERVER_ERROR,
                    "Error del Servidor",
                    "Nuestros servidores están experimentando problemas temporales. Por favor, intenta más tarde.",
                    technicalDetails,
                    true,
                    code
                );

            default:
                if (code >= 500) {
                    return new ErrorInfo(
                        ErrorType.SERVER_ERROR,
                        "Error del Servidor",
                        "Algo salió mal en nuestros servidores. Estamos trabajando para solucionarlo.",
                        technicalDetails,
                        true,
                        code
                    );
                } else {
                    return new ErrorInfo(
                        ErrorType.UNKNOWN_ERROR,
                        "Error Inesperado",
                        "Ocurrió un error inesperado (código " + code + "). Por favor, intenta nuevamente.",
                        technicalDetails,
                        true,
                        code
                    );
                }
        }
    }

    /**
     * Analiza una excepción de red y devuelve información del error
     */
    public static ErrorInfo analyzeNetworkException(Throwable throwable) {
        String technicalDetails = throwable.getClass().getSimpleName() + ": " + throwable.getMessage();

        if (throwable instanceof UnknownHostException || throwable instanceof ConnectException) {
            return new ErrorInfo(
                ErrorType.NETWORK_ERROR,
                "Sin Conexión",
                "No se pudo conectar al servidor. Verifica tu conexión a Internet y vuelve a intentarlo.",
                technicalDetails,
                true,
                0
            );
        } else if (throwable instanceof SocketTimeoutException) {
            return new ErrorInfo(
                ErrorType.TIMEOUT_ERROR,
                "Tiempo Agotado",
                "La solicitud está tardando demasiado. Verifica tu conexión e intenta nuevamente.",
                technicalDetails,
                true,
                0
            );
        } else {
            return new ErrorInfo(
                ErrorType.UNKNOWN_ERROR,
                "Error de Conexión",
                "Ocurrió un problema al comunicarse con el servidor: " + throwable.getMessage(),
                technicalDetails,
                true,
                0
            );
        }
    }

    /**
     * Muestra un diálogo de error con opción de reintentar
     */
    public static void showErrorDialog(
            Context context,
            ErrorInfo errorInfo,
            RetryCallback retryCallback
    ) {
        if (context == null) return;

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle(errorInfo.title);
        builder.setMessage(errorInfo.message);
        builder.setCancelable(false);

        // Botón de reintentar (si aplica)
        if (errorInfo.canRetry && retryCallback != null) {
            builder.setPositiveButton("Reintentar", (dialog, which) -> {
                dialog.dismiss();
                retryCallback.onRetry();
            });
            builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        } else {
            builder.setPositiveButton("Entendido", (dialog, which) -> dialog.dismiss());
        }

        // Botón de detalles técnicos (opcional, para debugging)
        if (errorInfo.technicalDetails != null && !errorInfo.technicalDetails.isEmpty()) {
            builder.setNeutralButton("Detalles", (dialog, which) -> {
                showTechnicalDetailsDialog(context, errorInfo);
            });
        }

        try {
            builder.show();
        } catch (Exception e) {
            Log.e(TAG, "Error al mostrar diálogo de error", e);
        }
    }

    /**
     * Muestra un diálogo simple de error sin opción de reintentar
     */
    public static void showErrorDialog(Context context, ErrorInfo errorInfo) {
        showErrorDialog(context, errorInfo, null);
    }

    /**
     * Muestra un diálogo con detalles técnicos (para debugging)
     */
    private static void showTechnicalDetailsDialog(Context context, ErrorInfo errorInfo) {
        new MaterialAlertDialogBuilder(context)
                .setTitle("Detalles Técnicos")
                .setMessage(errorInfo.technicalDetails)
                .setPositiveButton("Cerrar", null)
                .show();
    }

    /**
     * Maneja un error de respuesta HTTP y muestra el diálogo apropiado
     */
    public static void handleHttpError(
            Context context,
            Response<?> response,
            RetryCallback retryCallback
    ) {
        ErrorInfo errorInfo = analyzeHttpError(response);
        Log.e(TAG, "HTTP Error: " + errorInfo.technicalDetails);
        showErrorDialog(context, errorInfo, retryCallback);
    }

    /**
     * Maneja una excepción de red y muestra el diálogo apropiado
     */
    public static void handleNetworkException(
            Context context,
            Throwable throwable,
            RetryCallback retryCallback
    ) {
        ErrorInfo errorInfo = analyzeNetworkException(throwable);
        Log.e(TAG, "Network Exception: " + errorInfo.technicalDetails, throwable);
        showErrorDialog(context, errorInfo, retryCallback);
    }

    /**
     * Callback para el botón de reintentar
     */
    public interface RetryCallback {
        void onRetry();
    }

    /**
     * Crea un mensaje de error formateado para logging
     */
    public static String formatErrorForLog(String operation, ErrorInfo errorInfo) {
        return String.format(
                "[%s] %s - %s | Technical: %s",
                operation,
                errorInfo.title,
                errorInfo.message,
                errorInfo.technicalDetails
        );
    }

    /**
     * Verifica si un error requiere logout del usuario
     */
    public static boolean shouldLogout(ErrorInfo errorInfo) {
        return errorInfo.type == ErrorType.SESSION_EXPIRED;
    }
}

