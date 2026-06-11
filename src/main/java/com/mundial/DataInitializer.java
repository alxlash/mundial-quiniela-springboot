package com.mundial;

import com.mundial.entity.Equipo;
import com.mundial.entity.Partido;
import com.mundial.repository.EquipoRepository;
import com.mundial.repository.PartidoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final EquipoRepository equipoRepository;
    private final PartidoRepository partidoRepository;

    public DataInitializer(EquipoRepository equipoRepository, PartidoRepository partidoRepository) {
        this.equipoRepository = equipoRepository;
        this.partidoRepository = partidoRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. CARGAR EQUIPOS
        File archivoEquipos = new File("src/main/resources/equipos.csv");
        if (archivoEquipos.exists() && equipoRepository.count() == 0) {
            try (BufferedReader br = new BufferedReader(new FileReader(archivoEquipos))) {
                String linea;
                br.readLine(); // Brincar encabezado
                while ((linea = br.readLine()) != null) {
                    String[] datos = linea.split(",");
                    if (datos.length == 4) {
                        Equipo equipo = new Equipo();
                        equipo.setBanderaEmoji(datos[0].trim());
                        equipo.setNombre(datos[1].trim());
                        equipo.setGrupo(datos[2].trim());
                        equipo.setPosicionGrupo(datos[3].trim());
                        equipo.setPuntos(0);
                        equipo.setGolesAFavor(0);
                        equipo.setGolesEnContra(0);
                        equipoRepository.save(equipo);
                    }
                }
                System.out.println("✅ Equipos cargados correctamente.");
            }
        }

        // Si ya hay partidos en la base de datos, no volvemos a leer los CSV
        if (partidoRepository.count() > 0) {
            System.out.println("✨ Los partidos ya se encuentran cargados en la base de datos.");
            return;
        }

        // 2. CARGAR PARTIDOS DE GRUPOS
        File archivoGrupos = new File("src/main/resources/partidosGrupos.csv");
        if (archivoGrupos.exists()) {
            cargarArchivoPartidos(archivoGrupos, "GRUPOS");
        } else {
            System.err.println("⚠️ No se encontró el archivo partidosGrupos.csv");
        }

        // 3. CARGAR PARTIDOS CALIFICATORIOS (FASE FINAL)
        File archivoCalificatorios = new File("src/main/resources/partidosCalificatorios.csv");
        if (archivoCalificatorios.exists()) {
            cargarArchivoPartidos(archivoCalificatorios, "ELIMINATORIA");
        } else {
            System.err.println("⚠️ No se encontró el archivo partidosCalificatorios.csv");
        }
    }

    /**
     * Método auxiliar reutilizable para procesar la estructura de tus CSV de partidos
     */
    private void cargarArchivoPartidos(File archivo, String tipoFase) {
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            br.readLine(); // Brincamos la línea de encabezado del CSV
            
            int contador = 0;
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(",");
                if (datos.length == 8) {
                    Partido partido = new Partido();
                    partido.setNumeroPartido(Integer.parseInt(datos[0].trim()));
                    partido.setCodigoLocal(datos[1].trim());
                    partido.setCodigoVisitante(datos[2].trim());
                    partido.setFecha(LocalDate.parse(datos[3].trim()));
                    partido.setHora(LocalTime.parse(datos[4].trim()));
                    partido.setEstadio(datos[5].trim());
                    partido.setCiudad(datos[6].trim());
                    partido.setPaisSede(datos[7].trim());
                    partido.setFase(tipoFase); // 🔥 Asignamos si es GRUPOS o ELIMINATORIA
                    partido.setJugado(false);

                    partidoRepository.save(partido);
                    contador++;
                }
            }
            System.out.println("⚽ ¡Se cargaron " + contador + " partidos de tipo [" + tipoFase + "] desde " + archivo.getName() + "!");
        } catch (Exception e) {
            System.err.println("Error procesando el archivo " + archivo.getName() + ": " + e.getMessage());
        }
    }
}