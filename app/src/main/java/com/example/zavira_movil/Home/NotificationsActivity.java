package com.example.zavira_movil.Home;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.notifications.NotificationItem;
import com.example.zavira_movil.notifications.NotificationStorage;
import com.example.zavira_movil.notifications.NotificationsAdapter;

import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayout emptyState;
    private NotificationsAdapter adapter;
    private NotificationStorage notificationStorage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // Inicializar vistas
        ImageView btnBack = findViewById(R.id.btnBack);
        recyclerView = findViewById(R.id.recyclerViewNotifications);
        emptyState = findViewById(R.id.emptyState);

        // Configurar botón de retroceso
        btnBack.setOnClickListener(v -> finish());

        // Inicializar NotificationStorage
        notificationStorage = new NotificationStorage(this);
        
        // Limpiar notificaciones de prueba (solo en desarrollo)
        cleanTestNotifications();

        // Configurar RecyclerView
        setupRecyclerView();

        // Cargar notificaciones
        loadNotifications();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
    }

    private void loadNotifications() {
        List<NotificationItem> notifications = notificationStorage.getAllNotifications();

        if (notifications.isEmpty()) {
            // Mostrar estado vacío
            recyclerView.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            // Mostrar notificaciones
            recyclerView.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);

            adapter = new NotificationsAdapter(notifications, (notification, position) -> {
                // Marcar como leída al hacer clic
                notificationStorage.markAsRead(position);
                adapter.notifyItemChanged(position);
            });

            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar notificaciones al volver a la actividad
        loadNotifications();
    }
    
    /**
     * Limpia las notificaciones de prueba
     */
    private void cleanTestNotifications() {
        List<NotificationItem> notifications = notificationStorage.getAllNotifications();
        
        // Verificar si hay notificaciones de prueba
        boolean hasTestNotifications = false;
        for (NotificationItem notification : notifications) {
            if (notification.getTitle() != null && 
                (notification.getTitle().contains("Prueba") || 
                 notification.getMessage().contains("prueba para verificar el sistema"))) {
                hasTestNotifications = true;
                break;
            }
        }
        
        // Si hay notificaciones de prueba, limpiar todas
        if (hasTestNotifications) {
            notificationStorage.clearAll();
        }
    }
}
