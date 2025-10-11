package com.example.zavira_movil.remote;

import com.example.zavira_movil.BasicResponse;
import com.example.zavira_movil.PreguntaAcademica;
import com.example.zavira_movil.QuizCerrarRequest;
import com.example.zavira_movil.QuizInicialResponse;
import com.example.zavira_movil.model.AceptarRetoResponse;
import com.example.zavira_movil.model.EstadoRetoResponse;
import com.example.zavira_movil.model.HistorialResponse;
import com.example.zavira_movil.QuizResponse;
import com.example.zavira_movil.model.CerrarRequest;
import com.example.zavira_movil.model.CerrarResponse;
import com.example.zavira_movil.model.Estudiante;
import com.example.zavira_movil.model.KolbResultado;
import com.example.zavira_movil.model.LoginRequest;
import com.example.zavira_movil.model.OpponentBackend;
import com.example.zavira_movil.model.OpponentRaw;
import com.example.zavira_movil.model.RetoListItem;
import com.example.zavira_movil.model.SimulacroRequest;
import com.example.zavira_movil.model.SimulacroResponse;
import com.example.zavira_movil.model.IslaCerrarRequest;
import com.example.zavira_movil.model.IslaResumenResponse;
import com.example.zavira_movil.model.IslaCerrarResponse;
import com.example.zavira_movil.model.IslaSimulacroResponse;
import com.example.zavira_movil.model.IslaSimulacroRequest;
import com.example.zavira_movil.model.RankingResponse;
import com.example.zavira_movil.model.LogrosResponse;
import com.example.zavira_movil.model.OtorgarAreaRequest;
import com.example.zavira_movil.model.OtorgarAreaResponse;
import com.example.zavira_movil.model.LogrosTodosResponse;


import com.example.zavira_movil.model.KolbRequest;
import com.example.zavira_movil.model.KolbResponse;
import com.example.zavira_movil.model.MateriasResponse;
import com.example.zavira_movil.model.PreguntasKolb;
import com.example.zavira_movil.model.ResumenGeneral;
import com.example.zavira_movil.model.RetoCreadoResponse;
import com.example.zavira_movil.model.RetoCreateRequest;
import com.example.zavira_movil.model.RondaRequest;
import com.example.zavira_movil.model.RondaResponse;
import com.example.zavira_movil.model.ParadaRequest;
import com.example.zavira_movil.model.ParadaResponse;
import com.example.zavira_movil.model.PreguntasKolb;


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

    //Examen Inicial
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

    @GET("movil/oponentes")
    Call<List<OpponentBackend>> listarOponentes();

    // Crear reto (usa RetoCreateRequest con oponente_id)
    @POST("movil/retos")
    Call<RetoCreadoResponse> crearReto(@Body RetoCreateRequest body);

    // Aceptar reto
    @POST("movil/retos/{id}/aceptar")
    Call<AceptarRetoResponse> aceptarReto(@Path("id") String idReto);

    // Responder ronda
    @POST("movil/retos/ronda")
    Call<RondaResponse> responderRonda(@Body RondaRequest body);

    // OJO: tu ruta real es /movil/retos/:id/estado
    @GET("movil/retos/{id}/estado")
    Call<EstadoRetoResponse> estadoReto(@Path("id") String idReto);

    // NUEVO: listar retos (recibidos/pendientes del usuarios
    @GET("movil/retos")
    Call<List<RetoListItem>> listarRetos();

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
}