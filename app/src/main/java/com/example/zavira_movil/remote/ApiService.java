package com.example.zavira_movil.remote;

import com.example.zavira_movil.BasicResponse;
import com.example.zavira_movil.PreguntaAcademica;
import com.example.zavira_movil.QuizCerrarRequest;
import com.example.zavira_movil.QuizInicialResponse;
import com.example.zavira_movil.QuizResponse;
import com.example.zavira_movil.model.CerrarRequest;
import com.example.zavira_movil.model.CerrarResponse;
import com.example.zavira_movil.model.Estudiante;
import com.example.zavira_movil.model.KolbResultado;
import com.example.zavira_movil.model.LoginRequest;
import com.example.zavira_movil.model.SimulacroRequest;
import com.example.zavira_movil.model.SimulacroResponse;
import com.example.zavira_movil.model.IslaCerrarRequest;
import com.example.zavira_movil.model.IslaResumenResponse;
import com.example.zavira_movil.model.IslaCerrarResponse;
import com.example.zavira_movil.model.IslaSimulacroResponse;
import com.example.zavira_movil.model.IslaSimulacroRequest;


import com.example.zavira_movil.model.KolbRequest;
import com.example.zavira_movil.model.KolbResponse;
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
import retrofit2.http.Part;
import retrofit2.http.Path;

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

    //Test y Niveles por Area
    @POST("sesion/parada")
    Call<ParadaResponse> crearParada(@Body ParadaRequest body);

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

}