package com.mundial.view;

import com.mundial.entity.Usuario;
import com.mundial.repository.UsuarioRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import java.util.Optional;

// @Route("") le dice a Vaadin que esta es la pagina de inicio (http://localhost:8080)
@Route("")
public class LoginView extends VerticalLayout {

    private final UsuarioRepository usuarioRepository;

    public LoginView(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;

        // Configuración de la pantalla: centrar todo el contenido
        addClassName("login-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // Componentes visuales
        H1 titulo = new H1("🏆 Quiniela Mundial 2026 🏆");
        Paragraph instrucciones = new Paragraph("Ingresa tu nombre para entrar o registrarte:");
        
        TextField txtNombre = new TextField("Nombre de Usuario");
        txtNombre.setPlaceholder("Ej. Alex, Carlos, Juan...");
        txtNombre.setClearButtonVisible(true);
        txtNombre.setWidth("300px");

        Button btnEntrar = new Button("Entrar a la Quiniela");
        btnEntrar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnEntrar.setWidth("300px");

     // Lógica del botón al hacer clic
        btnEntrar.addClickListener(event -> {
            String nombre = txtNombre.getValue().trim();

            if (nombre.isEmpty()) {
                mostrarNotificacion("¡Por favor escribe un nombre!", NotificationVariant.LUMO_ERROR);
            } else {
                Optional<Usuario> usuarioOpt = usuarioRepository.findByNombre(nombre);
                Usuario usuarioActual;

                if (usuarioOpt.isPresent()) {
                    usuarioActual = usuarioOpt.get();
                    mostrarNotificacion("¡Bienvenido de vuelta, " + usuarioActual.getNombre() + "!", NotificationVariant.LUMO_SUCCESS);
                } else {
                    usuarioActual = new Usuario();
                    usuarioActual.setNombre(nombre);
                    usuarioActual.setPuntosQuiniela(0);
                    usuarioRepository.save(usuarioActual);
                    mostrarNotificacion("¡Registro exitoso! Bienvenido " + nombre, NotificationVariant.LUMO_SUCCESS);
                }

                // 🔥 EL TRUCO: Guardamos al usuario en la sesión del navegador para saber quién es
                VaadinSession.getCurrent().setAttribute(Usuario.class, usuarioActual);
                
                // Redireccionar a la pantalla de partidos usando su ruta
                getUI().ifPresent(ui -> ui.navigate("partidos"));
            }
        });

        // Agregar los componentes creados a la pantalla
        add(titulo, instrucciones, txtNombre, btnEntrar);
    }

    private void mostrarNotificacion(String mensaje, NotificationVariant variante) {
        Notification notificacion = Notification.show(mensaje, 3000, Notification.Position.MIDDLE);
        notificacion.addThemeVariants(variante);
    }
}