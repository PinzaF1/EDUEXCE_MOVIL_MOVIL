package com.example.zavira_movil.remote;

import com.example.zavira_movil.QuizCerrarRequest;
import com.example.zavira_movil.QuizInicialResponse;
import com.example.zavira_movil.model.AceptarRetoResponse;
import com.example.zavira_movil.model.EstadoRetoResponse;
import com.example.zavira_movil.model.Estudiante;
import com.example.zavira_movil.model.HistorialResponse;
import com.example.zavira_movil.model.KolbResultado;
import com.example.zavira_movil.model.LoginRequest;

import com.example.zavira_movil.model.KolbRequest;
import com.example.zavira_movil.model.KolbResponse;
import com.example.zavira_movil.model.MateriasResponse;
import com.example.zavira_movil.model.PreguntasKolb;
import com.example.zavira_movil.model.ResumenGeneral;
import com.example.zavira_movil.model.RetoCreadoResponse;
import com.example.zavira_movil.model.RetoCreateRequest;
import com.example.zavira_movil.model.RondaRequest;
import com.example.zavira_movil.model.RondaResponse;


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
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    //Logue Estudiante
    @POST("estudiante/login")
    Call<ResponseBody> loginEstudiante(@Body LoginRequest request);

    @GET("perfilEstudiante")
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




        // ---------- Progreso Móvil ----------
        @GET("movil/progreso/resumen")
        Call<ResumenGeneral> getResumen();

    @GET("movil/progreso/materias")
    Call<MateriasResponse> getMaterias();


    @GET("movil/progreso/historial")
    Call<HistorialResponse> getHistorial(
            @Query("page") int page,
            @Query("limit") int limit  );


        // retos 1 vs 1

    // 1) Crear reto
    @POST("movil/retos")
    Call<RetoCreadoResponse> crearReto(@Body RetoCreateRequest body);

    // 2) Aceptar reto -> devuelve reto, sesiones[], preguntas[]
    @POST("movil/retos/{id}/aceptar")
    Call<AceptarRetoResponse> aceptarReto(@Path("id") String idReto);

    // 3) Responder ronda -> { id_sesion, respuestas:[{orden, opcion}] }
    @POST("movil/retos/ronda")
    Call<RondaResponse> responderRonda(@Body RondaRequest body);

    // 4) Estado del reto -> { id_reto, estado, ganador, jugadores[] }
    @GET("movil/retos/{id}")
    Call<EstadoRetoResponse> estadoReto(@Path("id") String idReto);
}