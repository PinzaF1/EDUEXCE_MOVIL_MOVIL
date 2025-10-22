package com.example.zavira_movil.remote;

import com.example.zavira_movil.BasicResponse;
import com.example.zavira_movil.QuizCerrarRequest;
import com.example.zavira_movil.QuizInicialResponse;
import com.example.zavira_movil.retos1vs1.AceptarRetoResponse;
import com.example.zavira_movil.retos1vs1.EstadoRetoResponse;
import com.example.zavira_movil.progreso.HistorialResponse;
import com.example.zavira_movil.model.CerrarRequest;
import com.example.zavira_movil.model.CerrarResponse;
import com.example.zavira_movil.model.Estudiante;
import com.example.zavira_movil.model.KolbResultado;
import com.example.zavira_movil.model.LoginRequest;
import com.example.zavira_movil.model.SimulacroRequest;
import com.example.zavira_movil.niveleshome.SimulacroResponse;
import com.example.zavira_movil.model.IslaCerrarRequest;
import com.example.zavira_movil.model.IslaResumenResponse;
import com.example.zavira_movil.model.IslaCerrarResponse;
import com.example.zavira_movil.model.IslaSimulacroResponse;
import com.example.zavira_movil.model.IslaSimulacroRequest;
import com.example.zavira_movil.model.RankingResponse;
import com.example.zavira_movil.model.LogrosResponse;
import com.example.zavira_movil.model.OtorgarAreaRequest;
import com.example.zavira_movil.model.OtorgarAreaResponse;
import com.example.zavira_movil.retos1vs1.RetoListItem;
import com.example.zavira_movil.oponente.OpponentBackend;


import com.example.zavira_movil.model.KolbRequest;
import com.example.zavira_movil.model.KolbResponse;
import com.example.zavira_movil.progreso.MateriasResponse;
import com.example.zavira_movil.model.PreguntasKolb;
import com.example.zavira_movil.progreso.ResumenGeneral;
import com.example.zavira_movil.retos1vs1.RetoCreadoResponse;
import com.example.zavira_movil.retos1vs1.RetoCreateRequest;
import com.example.zavira_movil.model.RondaRequest;
import com.example.zavira_movil.model.RondaResponse;
import com.example.zavira_movil.niveleshome.ParadaRequest;
import com.example.zavira_movil.niveleshome.ParadaResponse;


import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    //Logue Estudiante
    @POST("estudiante/login")
    Call<ResponseBody> loginEstudiante(@Body LoginRequest request);
    @GET("estudiante/perfil")
    Call<Estudiante> getPerfilEstudiante();

    //Estilo de Kold
    @GET("kolb/preguntas")
    Call<List<PreguntasKolb>> getPreguntas();

    @POST("kolb/enviar")
    Call<KolbResponse> guardarRespuestas(@Body KolbRequest request);

    @GET("kolb/resultado")
    Call<KolbResultado> obtenerResultado();

    //Examen Inicial l
    @POST("quizz/iniciar")
    Call<QuizInicialResponse> iniciar(
            @Header("Authorization") String bearerToken,
            @Body Map<String, Object> body
    );

    @POST("quiz-inicial/cerrar")
    Call<ResponseBody> cerrar(
            @Header("Authorization") String bearerToken,
            @Body QuizCerrarRequest request
    );

    //MEtodos para la foto de perfil
    @Multipart
    @POST("users/me/photo")
    Call<ResponseBody> subirFoto(@Part MultipartBody.Part foto);

    @DELETE("users/me/photo")
    Call<ResponseBody> eliminarFoto();

    //Test y Niveles por Area
    @POST("sesion/parada")
    Call<ParadaResponse> crearParada(@Body ParadaRequest body);


    // ---------- Progreso Móvil ----------
    @GET("movil/progreso/resumen")
    Call<ResumenGeneral> getResumen();

    @GET("movil/progreso/materias")
    Call<MateriasResponse> getMaterias();


    @GET("movil/progreso/historial")
    Call<HistorialResponse> getHistorial(
            @Query("page") int page,
            @Query("limit") int limit);



    // === 1 vs 1 (tus rutas actuales) ===

    // Oponentes reales

    // Crear reto (usa RetoCreateRequest CON oponente_id)
    @POST("movil/retos")
    Call<RetoCreadoResponse> crearReto(@Body RetoCreateRequest body);

    // Aceptar reto (SIN body)
    @POST("movil/retos/{id}/aceptar")
    Call<AceptarRetoResponse> aceptarReto(@Path("id") String idReto);

    // Aceptar reto (CON body vacío) -> fallback cuando el server pide body
    @POST("movil/retos/{id}/aceptar")
    Call<AceptarRetoResponse> aceptarRetoConBody(@Path("id") String idReto, @Body Map<String, Object> empty);

    // Responder ronda
    @POST("movil/retos/ronda")
    Call<RondaResponse> responderRonda(@Body RondaRequest body);

    // Estado del reto
    @GET("movil/retos/{id}/estado")
    Call<EstadoRetoResponse> estadoReto(@Path("id") String idReto);
    @POST("sesion/cerrar")
    Call<CerrarResponse> cerrarSesion(@Body CerrarRequest request);

    @POST("movil/simulacro")
    Call<SimulacroResponse> crearSimulacro(@Body SimulacroRequest request);

    @POST("movil/simulacro/cerrar")
    Call<CerrarResponse> cerrarSimulacro(@Body CerrarRequest request);

    // Iniciar simulacro
    @POST("movil/isla/simulacro")
    Call<IslaSimulacroResponse> iniciarIslaSimulacro(@Body IslaSimulacroRequest req);

    // Cerrar simulacro
    @POST("movil/isla/simulacro/cerrar")
    Call<IslaCerrarResponse> cerrarIslaSimulacro(@Body IslaCerrarRequest req);

    // Resumen
    @GET("movil/isla/simulacro/{id}/resumen")
    Call<IslaResumenResponse> getIslaResumen(@Path("id") int idSesion);


    @GET("movil/ranking")
    Call<RankingResponse> getRanking();

    @GET("movil/logros")
    Call<LogrosResponse> getMisLogros();

    @POST("movil/logros/otorgar-area")
    Call<OtorgarAreaResponse> otorgarInsigniaArea(@Body OtorgarAreaRequest body);

    // AHORA (POST)
    @POST("movil/password")
    Call<BasicResponse> cambiarPasswordMovil(@Body com.example.zavira_movil.model.CambiarPassword body);

    @PUT("movil/perfil/{id}")
    Call<com.example.zavira_movil.Perfil.EditarPerfilResponse> editarPerfil(
            @Path("id") int id,
            @Body com.example.zavira_movil.Perfil.EditarPerfilRequest body
    );

    @GET("movil/retos")
    Call<List<RetoListItem>> listarRetos();
    @GET("movil/retos/oponentes")
    Call<List<OpponentBackend>> listarOponentes();
}