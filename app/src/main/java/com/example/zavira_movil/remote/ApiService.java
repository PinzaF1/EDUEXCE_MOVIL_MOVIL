package com.example.zavira_movil.remote;

import com.example.zavira_movil.BasicResponse;
import com.example.zavira_movil.HislaConocimiento.IslaSimulacroRequest;
import com.example.zavira_movil.HislaConocimiento.IslaSimulacroResponse;
import com.example.zavira_movil.QuizCerrarRequest;
import com.example.zavira_movil.QuizInicialResponse;

import com.example.zavira_movil.progreso.DiagnosticoInicial;
import com.example.zavira_movil.retos1vs1.AceptarRetoResponse;
import com.example.zavira_movil.retos1vs1.EstadoRetoResponse;
import com.example.zavira_movil.progreso.HistorialResponse;
import com.example.zavira_movil.niveleshome.CerrarRequest;
import com.example.zavira_movil.niveleshome.CerrarResponse;

import com.example.zavira_movil.model.Estudiante;
import com.example.zavira_movil.HislaConocimiento.IslaCerrarRequest;
import com.example.zavira_movil.HislaConocimiento.IslaCerrarResponse;
import com.example.zavira_movil.HislaConocimiento.IslaResumenResponse;
import com.example.zavira_movil.model.KolbRequest;
import com.example.zavira_movil.model.KolbResponse;
import com.example.zavira_movil.model.LoginRequest;
import com.example.zavira_movil.model.OtorgarAreaRequest;
import com.example.zavira_movil.model.OtorgarAreaResponse;

import com.example.zavira_movil.model.RankingResponse;
import com.example.zavira_movil.retos1vs1.RondaResponse;
import com.example.zavira_movil.model.SimulacroRequest;
import com.example.zavira_movil.niveleshome.CerrarRequest;
import com.example.zavira_movil.niveleshome.CerrarResponse;
import com.example.zavira_movil.niveleshome.ParadaRequest;
import com.example.zavira_movil.niveleshome.ParadaResponse;
import com.example.zavira_movil.niveleshome.SimulacroResponse;

import com.example.zavira_movil.retos1vs1.MarcadorResponse;
import com.example.zavira_movil.retos1vs1.RetoListItem;

import com.example.zavira_movil.oponente.OpponentBackend;
import com.example.zavira_movil.progreso.HistorialResponse;
import com.example.zavira_movil.progreso.MateriasResponse;
import com.example.zavira_movil.progreso.ResumenGeneral;
import com.example.zavira_movil.retos1vs1.AceptarRetoResponse;
import com.example.zavira_movil.retos1vs1.EstadoRetoResponse;
import com.example.zavira_movil.retos1vs1.RetoCreadoResponse;
import com.example.zavira_movil.retos1vs1.RetoCreateRequest;

import com.example.zavira_movil.retos1vs1.RetoListItem;
import com.example.zavira_movil.model.LogrosResponse;
import com.example.zavira_movil.HislaConocimiento.IslaSimulacroRequest;
import com.example.zavira_movil.HislaConocimiento.IslaSimulacroResponse;
import com.example.zavira_movil.HislaConocimiento.IslaCerrarRequest;
import com.example.zavira_movil.HislaConocimiento.IslaCerrarResultadoResponse;
import com.example.zavira_movil.HislaConocimiento.IslaResumenResponse;
import com.example.zavira_movil.HislaConocimiento.IslaCerrarMixtoRequest;
import com.example.zavira_movil.HislaConocimiento.IslaCerrarRequest;
import com.example.zavira_movil.HislaConocimiento.IslaCerrarResultadoResponse;
import com.example.zavira_movil.HislaConocimiento.IslaResumenResponse;
import com.example.zavira_movil.HislaConocimiento.IslaSimulacroRequest;
import com.example.zavira_movil.HislaConocimiento.IslaSimulacroResponse;
import com.example.zavira_movil.HislaConocimiento.IslaIniciarRequest;

import com.example.zavira_movil.retos1vs1.RondaResponse;
import com.example.zavira_movil.niveleshome.ParadaRequest;
import com.example.zavira_movil.niveleshome.ParadaResponse;
import com.example.zavira_movil.retos1vs1.RondaRequest;


import java.util.List;
import java.util.Map;


import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // ---------- Autenticación / Perfil ----------
    @POST("estudiante/login")
    Call<ResponseBody> loginEstudiante(@Body LoginRequest request);

    @GET("estudiante/perfil")
    Call<Estudiante> getPerfilEstudiante();

    // ---------- Kolb ----------
    @GET("kolb/preguntas")
    Call<List<com.example.zavira_movil.model.PreguntasKolb>> getPreguntas();

    @POST("kolb/enviar")
    Call<KolbResponse> guardarRespuestas(@Body KolbRequest request);

    @GET("kolb/resultado")
    Call<com.example.zavira_movil.model.KolbResultado> obtenerResultado();

    // ---------- Examen inicial ----------
    @POST("quizz/iniciar")
    Call<QuizInicialResponse> iniciar(
            @Header("Authorization") String bearerToken,
            @Body Map<String, Object> body
    );

    @POST("quizz/cerrar")
    Call<com.example.zavira_movil.QuizCerrarResponse> cerrar(
            @Header("Authorization") String bearerToken,
            @Body QuizCerrarRequest request
    );

    // NUEVO: progreso del diagnóstico (cuando tengas el JSON exacto, creamos el modelo)
    @GET("quizz/progreso")
    Call<DiagnosticoInicial> diagnosticoProgreso();


    // ---------- Foto de perfil ----------
    @Multipart
    @POST("users/me/photo")
    Call<ResponseBody> subirFoto(@Part MultipartBody.Part foto);

    @DELETE("users/me/photo")
    Call<ResponseBody> eliminarFoto();

    // ---------- Niveles / Sesiones por área ----------
    @POST("sesion/parada")
    Call<ParadaResponse> crearParada(@Body ParadaRequest body);

    @POST("sesion/cerrar")
    Call<CerrarResponse> cerrarSesion(@Body CerrarRequest request);

    @POST("sesion/cerrar")
    Call<CerrarResponse> cerrarSesionCompat(@Body Map<String, Object> bodyCompat);

    // ---------- Simulacro ----------
    @POST("movil/simulacro")
    Call<SimulacroResponse> crearSimulacro(@Body SimulacroRequest request);

    @POST("movil/simulacro/cerrar")
    Call<com.example.zavira_movil.niveleshome.CerrarResponse> cerrarSimulacro(@Body com.example.zavira_movil.niveleshome.CerrarRequest request);

    @POST("movil/isla/simulacro")
    Call<IslaSimulacroResponse> iniciarIslaSimulacro(@Body IslaSimulacroRequest body);

    @POST("movil/isla/simulacro/cerrar")
    Call<IslaCerrarResultadoResponse> cerrarIslaSimulacro(@Body IslaCerrarRequest body);



    // ---------- Isla ----------
    // Iniciar simulacro: POST /movil/isla/simulacro  => body: {"modalidad":"facil"|"dificil"}
    @POST("movil/isla/simulacro")
    Call<IslaSimulacroResponse> iniciarIslaSimulacro(
            @Header("Authorization") String authBearer,
            @Body IslaIniciarRequest body
    );

    // Cerrar simulacro: POST /movil/isla/simulacro/cerrar  => body: {id_sesion, respuestas:[{id_pregunta,respuesta}]}
    @POST("movil/isla/simulacro/cerrar")
    Call<IslaCerrarResultadoResponse> cerrarIslaSimulacro(
            @Header("Authorization") String authBearer,
            @Body IslaCerrarRequest body
    );

    @GET("movil/isla/simulacro/{id}/resumen")
    Call<IslaResumenResponse> getIslaResumen(@Path("id") int idSesion);

    // ---------- Progreso ----------
    @GET("movil/progreso/resumen")
    Call<ResumenGeneral> getResumen();

    @GET("movil/progreso/materias")
    Call<MateriasResponse> getMaterias();

    @GET("movil/progreso/historial")
    Call<HistorialResponse> getHistorial(@Query("page") int page, @Query("limit") int limit);

    // ---------- 1 vs 1 ----------
    @POST("movil/retos")
    Call<RetoCreadoResponse> crearReto(@Body RetoCreateRequest body);

    @POST("movil/retos/{id}/aceptar")
    Call<AceptarRetoResponse> aceptarReto(@Path("id") String idReto);

    @POST("movil/retos/{id}/aceptar")
    Call<AceptarRetoResponse> aceptarRetoConBody(
        @Path("id") String idReto, 
        @Body Map<String, Object> syncData
    );

    @POST("movil/retos/{id}/rechazar")
    Call<BasicResponse> rechazarReto(@Path("id") String idReto);

    @POST("movil/retos/ronda")
    Call<RondaResponse> responderRonda(@Body RondaRequest body);

    @GET("movil/retos/{id}/estado")
    Call<EstadoRetoResponse> estadoReto(@Path("id") String idReto);

    @GET("movil/retos")
    Call<List<RetoListItem>> listarRetos(@Query("tipo") String tipo);

    @GET("movil/retos/oponentes")
    Call<List<OpponentBackend>> listarOponentes();
    
    @DELETE("movil/retos/{id}/abandonar")
    Call<ResponseBody> abandonarReto(@Path("id") String idReto);


    // Marcador por usuario (token)
    @GET("movil/retos/marcador")
    Call<MarcadorResponse> marcador();

    // Marcador por sesión (si el backend lo soporta como query param; si no, simplemente lo ignorará)
    @GET("movil/retos/marcador")
    Call<MarcadorResponse> marcadorPorSesion(@Query("id_sesion") Integer idSesion);




    // ---------- Ranking / Logros ----------
    @GET("movil/ranking")
    Call<RankingResponse> getRanking();

    @GET("movil/logros")
    Call<LogrosResponse> getMisLogros();

    @POST("movil/logros/otorgar-area")
    Call<OtorgarAreaResponse> otorgarInsigniaArea(@Body OtorgarAreaRequest body);

    // ---------- Perfil ----------
    @POST("movil/password")
    Call<BasicResponse> cambiarPasswordMovil(@Body com.example.zavira_movil.model.CambiarPassword body);

    @PUT("movil/perfil/{id}")
    Call<com.example.zavira_movil.Perfil.EditarPerfilResponse> editarPerfil(
            @Path("id") int id,
            @Body com.example.zavira_movil.Perfil.EditarPerfilRequest body
    );

    // ---------- Recuperación de Contraseña ----------
    @POST("estudiante/recuperar/solicitar")
    Call<BasicResponse> solicitarCodigoRecuperacion(@Body com.example.zavira_movil.resetpassword.SolicitarCodigoRequest body);

    @POST("estudiante/recuperar/verificar")
    Call<BasicResponse> verificarCodigoRecuperacion(@Body com.example.zavira_movil.resetpassword.VerificarCodigoRequest body);

    @POST("estudiante/recuperar/restablecer")
    Call<BasicResponse> restablecerPassword(@Body com.example.zavira_movil.resetpassword.RestablecerPasswordRequest body);

    // ---------- Sincronización de Progreso (Niveles y Vidas) ----------
    @GET("movil/sincronizacion/progreso")
    Call<com.example.zavira_movil.sincronizacion.SincronizacionResponse> obtenerProgresoSincronizacion();

    @POST("movil/sincronizacion/nivel")
    Call<BasicResponse> actualizarNivelDesbloqueado(@Body com.example.zavira_movil.sincronizacion.ActualizarNivelRequest body);

    @POST("movil/sincronizacion/vidas")
    Call<BasicResponse> actualizarVidas(@Body com.example.zavira_movil.sincronizacion.ActualizarVidasRequest body);

    @POST("movil/sincronizacion/todo")
    Call<BasicResponse> sincronizarProgreso(@Body com.example.zavira_movil.sincronizacion.SincronizarTodoRequest body);

    // ---------- Notificaciones FCM ----------
    @POST("movil/fcm/register")
    Call<Void> registerFCMToken(@Body okhttp3.RequestBody body);
}
