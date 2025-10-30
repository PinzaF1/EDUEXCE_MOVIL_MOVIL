package com.example.zavira_movil.HislaConocimiento;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Body para POST /movil/isla/simulacro/cerrar (formato Postman):
 * {
 *   "id_sesion": 1507,
 *   "respuestas": [
 *     { "id_pregunta": 2076, "respuesta": "B", "tiempo_empleado_seg": 15 },
 *     { "orden": 2, "opcion": "C", "tiempo_empleado_seg": 12 }
 *   ]
 * }
 */
public class IslaCerrarMixtoRequest {

    @SerializedName("id_sesion")
    public int idSesion;

    @SerializedName("respuestas")
    public List<Resp> respuestas;

    public IslaCerrarMixtoRequest(int idSesion, List<Resp> respuestas) {
        this.idSesion = idSesion;
        this.respuestas = respuestas;
    }

    /** Item de respuesta. Admite dos variantes: por id_pregunta o por orden. */
    public static class Resp {
        // Variante por id_pregunta
        @SerializedName("id_pregunta") public Integer id_pregunta;
        @SerializedName("respuesta")   public String  respuesta;

        // Variante por orden/opcion
        @SerializedName("orden")  public Integer orden;
        @SerializedName("opcion") public String  opcion;

        // Campo opcional en ambos casos
        @SerializedName("tiempo_empleado_seg") public Integer tiempo_empleado_seg;

        /** Constructor para variante por id_pregunta */
        public Resp(Integer id_pregunta, String respuesta, Integer tiempo_empleado_seg) {
            this.id_pregunta = id_pregunta;
            this.respuesta = respuesta;
            this.tiempo_empleado_seg = tiempo_empleado_seg;
        }

        /** Factory para variante por orden/opcion */
        public static Resp porOrden(int orden, String opcion, Integer tiempo) {
            Resp r = new Resp(null, null, tiempo);
            r.orden = orden;
            r.opcion = opcion;
            return r;
        }
    }
}
