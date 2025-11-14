package com.example.zavira_movil.Home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.notifications.NotificationItem;
import com.example.zavira_movil.notifications.NotificationStorage;
import com.example.zavira_movil.notifications.NotificationsAdapter;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayout emptyState;
    private NotificationsAdapter adapter;
    private NotificationStorage notificationStorage;
    private TextView tvMarkAllAsRead;
    private TextView tvNotificationCount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // Inicializar vistas
        ImageView btnBack = findViewById(R.id.btnBack);
        recyclerView = findViewById(R.id.recyclerViewNotifications);
        emptyState = findViewById(R.id.emptyState);
        tvMarkAllAsRead = findViewById(R.id.tvMarkAllAsRead);
        tvNotificationCount = findViewById(R.id.tvNotificationCount);

        // Configurar botón de retroceso
        btnBack.setOnClickListener(v -> {
            finish();
            // Notificar a HomeActivity para que actualice el badge
            sendBadgeUpdateBroadcast();
        });

        // Inicializar NotificationStorage
        notificationStorage = new NotificationStorage(this);
        
        // Limpiar notificaciones de prueba (solo en desarrollo)
        cleanTestNotifications();

        // Configurar botón "Marcar todas como leídas"
        if (tvMarkAllAsRead != null) {
            tvMarkAllAsRead.setOnClickListener(v -> markAllAsRead());
        }

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

        // Actualizar contador
        updateNotificationCount(notifications);

        if (notifications.isEmpty()) {
            // Mostrar estado vacío
            recyclerView.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
            if (tvMarkAllAsRead != null) {
                tvMarkAllAsRead.setVisibility(View.GONE);
            }
        } else {
            // Mostrar notificaciones
            recyclerView.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);

            // Mostrar/ocultar botón "Marcar todas como leídas"
            if (tvMarkAllAsRead != null) {
                int unreadCount = notificationStorage.getUnreadCount();
                tvMarkAllAsRead.setVisibility(unreadCount > 0 ? View.VISIBLE : View.GONE);
            }

            if (adapter == null) {
                adapter = new NotificationsAdapter(notifications, (notification, position) -> {
                    // Marcar como leída al hacer clic
                    if (!notification.isRead()) {
                        notificationStorage.markAsRead(position);
                        notification.setRead(true);
                        adapter.notifyItemChanged(position);

                        // Actualizar contador y botón
                        updateNotificationCount(notificationStorage.getAllNotifications());
                        updateMarkAllButton();

                        // Notificar a HomeActivity
                        sendBadgeUpdateBroadcast();
                    }
                });
                recyclerView.setAdapter(adapter);
            } else {
                adapter.updateNotifications(notifications);
            }
        }
    }

    /**
     * Marca todas las notificaciones como leídas
     */
    private void markAllAsRead() {
        notificationStorage.markAllAsRead();
        loadNotifications();

        // Notificar a HomeActivity para que actualice el badge
        sendBadgeUpdateBroadcast();

        // Mostrar feedback visual
        if (tvMarkAllAsRead != null) {
            tvMarkAllAsRead.setText("✓ Todas leídas");
            tvMarkAllAsRead.setEnabled(false);
            tvMarkAllAsRead.postDelayed(() -> {
                tvMarkAllAsRead.setText("Marcar todas como leídas");
                tvMarkAllAsRead.setEnabled(true);
            }, 1500);
        }
    }

    /**
     * Actualiza el contador de notificaciones
     */
    private void updateNotificationCount(List<NotificationItem> notifications) {
        if (tvNotificationCount != null) {
            int unreadCount = 0;
            for (NotificationItem notification : notifications) {
                if (!notification.isRead()) {
                    unreadCount++;
                }
            }

            if (unreadCount > 0) {
                tvNotificationCount.setVisibility(View.VISIBLE);
                tvNotificationCount.setText(unreadCount + " sin leer");
            } else {
                tvNotificationCount.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Actualiza la visibilidad del botón "Marcar todas como leídas"
     */
    private void updateMarkAllButton() {
        if (tvMarkAllAsRead != null) {
            int unreadCount = notificationStorage.getUnreadCount();
            tvMarkAllAsRead.setVisibility(unreadCount > 0 ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Envía broadcast para actualizar el badge en HomeActivity
     */
    private void sendBadgeUpdateBroadcast() {
        Intent intent = new Intent("com.example.zavira_movil.UPDATE_NOTIFICATION_BADGE");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar notificaciones al volver a la actividad
        loadNotifications();
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Notificar a HomeActivity para que actualice el badge
        sendBadgeUpdateBroadcast();
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
