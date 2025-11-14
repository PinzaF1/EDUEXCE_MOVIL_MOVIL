package com.example.zavira_movil.notifications;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;

import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {
    
    private List<NotificationItem> notifications;
    private OnNotificationClickListener listener;
    
    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationItem notification, int position);
    }
    
    public NotificationsAdapter(List<NotificationItem> notifications, OnNotificationClickListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationItem notification = notifications.get(position);
        holder.bind(notification, position);
    }
    
    @Override
    public int getItemCount() {
        return notifications.size();
    }
    
    class NotificationViewHolder extends RecyclerView.ViewHolder {
        private View iconBackground;
        private ImageView notificationIcon;
        private TextView notificationTitle;
        private TextView notificationMessage;
        private View notificationInfoContainer;
        private TextView notificationArea;
        private TextView notificationScore;
        private TextView notificationTime;
        private View unreadIndicator;
        
        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            iconBackground = itemView.findViewById(R.id.iconBackground);
            notificationIcon = itemView.findViewById(R.id.notificationIcon);
            notificationTitle = itemView.findViewById(R.id.notificationTitle);
            notificationMessage = itemView.findViewById(R.id.notificationMessage);
            notificationInfoContainer = itemView.findViewById(R.id.notificationInfoContainer);
            notificationArea = itemView.findViewById(R.id.notificationArea);
            notificationScore = itemView.findViewById(R.id.notificationScore);
            notificationTime = itemView.findViewById(R.id.notificationTime);
            unreadIndicator = itemView.findViewById(R.id.unreadIndicator);
        }
        
        public void bind(NotificationItem notification, int position) {
            notificationTitle.setText(notification.getTitle());
            notificationMessage.setText(notification.getMessage());
            notificationTime.setText(notification.getTimeAgo());
            
            // Configurar ícono y color según el tipo
            NotificationStyle style = getNotificationStyle(notification);
            notificationIcon.setImageResource(style.iconRes);
            iconBackground.setBackgroundResource(style.backgroundRes);
            
            // Mostrar información adicional si está disponible
            if (notification.getArea() != null && notification.getPuntaje() != null) {
                notificationInfoContainer.setVisibility(View.VISIBLE);
                notificationArea.setText(notification.getArea());
                notificationScore.setText(notification.getPuntaje() + "%");
                
                // Color del chip de puntaje según el valor
                int puntaje = Integer.parseInt(notification.getPuntaje());
                if (puntaje < 40) {
                    notificationScore.setBackgroundResource(R.drawable.bg_chip_red);
                    notificationScore.setTextColor(Color.parseColor("#EF4444"));
                } else if (puntaje < 70) {
                    notificationScore.setBackgroundResource(R.drawable.bg_chip_orange);
                    notificationScore.setTextColor(Color.parseColor("#F59E0B"));
                } else {
                    notificationScore.setBackgroundResource(R.drawable.bg_chip_green);
                    notificationScore.setTextColor(Color.parseColor("#22C55E"));
                }
            } else {
                notificationInfoContainer.setVisibility(View.GONE);
            }
            
            // Indicador de no leído
            if (notification.isRead()) {
                unreadIndicator.setVisibility(View.GONE);
                itemView.setAlpha(0.7f); // Más transparente para notificaciones leídas
                itemView.setBackgroundResource(android.R.color.transparent);
            } else {
                unreadIndicator.setVisibility(View.VISIBLE);
                itemView.setAlpha(1.0f); // Opaco para notificaciones no leídas
                itemView.setBackgroundResource(R.drawable.bg_notification_unread);
            }
            
            // Click listener con animación
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    // Animación de feedback
                    itemView.animate()
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(100)
                        .withEndAction(() -> {
                            itemView.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(100)
                                .start();
                        })
                        .start();

                    listener.onNotificationClick(notification, position);
                }
            });
        }
        
        private NotificationStyle getNotificationStyle(NotificationItem notification) {
            String tipo = notification.getTipo();
            String puntajeStr = notification.getPuntaje();
            
            // Estilo según el tipo de notificación
            if ("puntaje_bajo_inmediato".equals(tipo)) {
                return new NotificationStyle(R.drawable.ic_notification_alert, R.drawable.bg_circle_gray);
            } else if ("recordatorio_practica".equals(tipo)) {
                return new NotificationStyle(R.drawable.ic_notification_warning, R.drawable.bg_circle_gray);
            } else if ("logro_desbloqueado".equals(tipo)) {
                return new NotificationStyle(R.drawable.ic_notification_success, R.drawable.bg_circle_gray);
            }
            
            // Estilo según el puntaje si está disponible
            if (puntajeStr != null) {
                try {
                    int puntaje = Integer.parseInt(puntajeStr);
                    if (puntaje < 40) {
                        return new NotificationStyle(R.drawable.ic_notification_alert, R.drawable.bg_circle_gray);
                    } else if (puntaje < 70) {
                        return new NotificationStyle(R.drawable.ic_notification_warning, R.drawable.bg_circle_gray);
                    } else {
                        return new NotificationStyle(R.drawable.ic_notification_success, R.drawable.bg_circle_gray);
                    }
                } catch (NumberFormatException e) {
                    // Ignorar si no se puede parsear
                }
            }
            
            return new NotificationStyle(R.drawable.ic_notification_info, R.drawable.bg_circle_gray);
        }
    }
    
    // Clase auxiliar para estilos de notificación
    private static class NotificationStyle {
        int iconRes;
        int backgroundRes;
        
        NotificationStyle(int iconRes, int backgroundRes) {
            this.iconRes = iconRes;
            this.backgroundRes = backgroundRes;
        }
    }
    
    public void updateNotifications(List<NotificationItem> newNotifications) {
        this.notifications = newNotifications;
        notifyDataSetChanged();
    }
}
