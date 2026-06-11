package com.mundial.view;

import com.mundial.entity.Usuario;
import com.mundial.repository.UsuarioRepository;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

@Route("") // Ruta raíz: lo primero que verán al abrir http://localhost:8080/
public class LoginRegistroView extends VerticalLayout {

    private final UsuarioRepository usuarioRepository;

    private Tabs tabsModo;
    private Tab tabLogin;
    private Tab tabRegistro;

    private TextField txtNombre;
    private PasswordField txtPassword;
    private Button btnAccion;
    private Span lblSubtitulo;

    public LoginRegistroView(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;

        // Centrar todo el contenido en la pantalla como un login profesional
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);

        // Contenedor del Formulario (una tarjeta blanca con sombra)
        VerticalLayout tarjetaFormulario = new VerticalLayout();
        tarjetaFormulario.setWidth("380px");
        tarjetaFormulario.setPadding(true);
        tarjetaFormulario.setSpacing(true);
        tarjetaFormulario.getStyle()
                .set("background-color", "#ffffff")
                .set("border-radius", "12px")
                .set("box-shadow", "0 4px 12px rgba(0, 0, 0, 0.1)");

        // Encabezado
        H2 titulo = new H2("🏆 Quiniela Mundial 🏆");
        titulo.getStyle().set("margin", "0");
        lblSubtitulo = new Span("Introduce tus credenciales para ingresar.");
        lblSubtitulo.getStyle().set("color", "#64748b").set("font-size", "14px");

        // Pestañas para cambiar entre Login y Registro
        tabLogin = new Tab("Iniciar Sesión");
        tabRegistro = new Tab("Registrarse");
        tabsModo = new Tabs(tabLogin, tabRegistro);
        tabsModo.setWidthFull();

        // Campos de texto
        txtNombre = new TextField("Tu Apodo / Nombre");
        txtNombre.setPlaceholder("Ej. Alex99");
        txtNombre.setWidthFull();
        txtNombre.setPrefixComponent(VaadinIcon.USER.create());

        txtPassword = new PasswordField("Contraseña");
        txtPassword.setPlaceholder("••••••••");
        txtPassword.setWidthFull();
        txtPassword.setPrefixComponent(VaadinIcon.LOCK.create());

        // Botón de acción principal
        btnAccion = new Button("Ingresar", VaadinIcon.SIGN_IN.create());
        btnAccion.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnAccion.setWidthFull();

        // Armar la tarjeta
        tarjetaFormulario.add(titulo, lblSubtitulo, tabsModo, txtNombre, txtPassword, btnAccion);
        add(tarjetaFormulario);

        // OYENTES DE EVENTOS (Listeners)
        
        // 1. Cambiar el diseño visual según la pestaña seleccionada
        tabsModo.addSelectedChangeListener(event -> alternarModoFormulario());

        // 2. Ejecutar la lógica al dar clic al botón
        btnAccion.addClickListener(e -> procesarFormulario());
    }

    private void alternarModoFormulario() {
        if (tabsModo.getSelectedTab() == tabLogin) {
            lblSubtitulo.setText("Introduce tus credenciales para ingresar.");
            btnAccion.setText("Ingresar");
            btnAccion.setIcon(VaadinIcon.SIGN_IN.create());
            btnAccion.removeThemeVariants(ButtonVariant.LUMO_SUCCESS);
            btnAccion.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        } else {
            lblSubtitulo.setText("Elige un apodo único y una contraseña.");
            btnAccion.setText("Crear mi Cuenta");
            btnAccion.setIcon(VaadinIcon.USER_CHECK.create());
            btnAccion.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            btnAccion.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        }
    }

    private void procesarFormulario() {
        String nombre = txtNombre.getValue().trim();
        String password = txtPassword.getValue();

        // Validación de campos vacíos
        if (nombre.isEmpty() || password.isEmpty()) {
            mostrarNotificacion("⚠️ Por favor, rellena todos los campos.", NotificationVariant.LUMO_ERROR);
            return;
        }

        if (tabsModo.getSelectedTab() == tabLogin) {
            // LÓGICA DE INICIO DE SESIÓN
            usuarioRepository.findByNombre(nombre).ifPresentOrElse(usuario -> {
                // 🛑 NOTA SEGURIDAD: En producción comparar con codificador (BCrypt)
                // Por ahora, validación directa por simplicidad práctica
                if (usuario.getPassword().equals(password)) {
                    iniciarSesionExitoso(usuario);
                } else {
                    mostrarNotificacion("❌ Contraseña incorrecta. Inténtalo de nuevo.", NotificationVariant.LUMO_ERROR);
                }
            }, () -> {
                mostrarNotificacion("❓ No existe ningún usuario con ese apodo.", NotificationVariant.LUMO_ERROR);
            });

        } else {
            // LÓGICA DE REGISTRO
            if (usuarioRepository.existsByNombre(nombre)) {
                mostrarNotificacion("🚫 ¡Ups! Ese apodo ya está tomado. Intenta con otro.", NotificationVariant.LUMO_WARNING);
            } else {
                // Creamos el nuevo usuario con sus puntos en 0
                Usuario nuevoUsuario = new Usuario(nombre, password);
                Usuario guardado = usuarioRepository.save(nuevoUsuario);
                
                mostrarNotificacion("🎉 ¡Cuenta creada con éxito!", NotificationVariant.LUMO_SUCCESS);
                iniciarSesionExitoso(guardado);
            }
        }
    }

    private void iniciarSesionExitoso(Usuario usuario) {
        // 🔥 LA CLAVE: Guardamos el ID del usuario en la sesión global del navegador
        VaadinSession.getCurrent().setAttribute("USUARIO_ID", usuario.getId());
        VaadinSession.getCurrent().setAttribute("USUARIO_NOMBRE", usuario.getNombre());

        // Redirigimos al Dashboard / Bienvenida (Crearemos esta ruta a continuación)
        UI.getCurrent().navigate(BienvenidaView.class);
    }

    private void mostrarNotificacion(String mensaje, NotificationVariant variante) {
        Notification notif = Notification.show(mensaje, 3000, Notification.Position.TOP_CENTER);
        notif.addThemeVariants(variante);
    }
}