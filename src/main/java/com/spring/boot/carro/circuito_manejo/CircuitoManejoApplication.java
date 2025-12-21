package com.spring.boot.carro.circuito_manejo;

import com.spring.boot.carro.circuito_manejo.persistence.entity.Paquete;
import com.spring.boot.carro.circuito_manejo.persistence.entity.Usuario;
import com.spring.boot.carro.circuito_manejo.persistence.entity.Vehiculo;
import com.spring.boot.carro.circuito_manejo.persistence.enums.EstadoVehiculosEnum;
import com.spring.boot.carro.circuito_manejo.persistence.enums.TipoDocumentoEnum;
import com.spring.boot.carro.circuito_manejo.persistence.enums.TipoTransmisionEnum;
import com.spring.boot.carro.circuito_manejo.persistence.repository.PaqueteRepository;
import com.spring.boot.carro.circuito_manejo.persistence.repository.UsuarioRepository;
import com.spring.boot.carro.circuito_manejo.persistence.repository.VehiculoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@EnableScheduling
@SpringBootApplication
public class CircuitoManejoApplication {

	public static void main(String[] args) {
		SpringApplication.run(CircuitoManejoApplication.class, args);
	}


	// Clase auxiliar para agrupar los datos de cada usuario
	private record DatosUsuario(
			String nombre,
			String apellido,
			String email,
			String numeroDocumento,
			String telefono,
			TipoDocumentoEnum tipoDocumento
	) {}

	@Bean
	CommandLineRunner init(UsuarioRepository usuarioRepository, PaqueteRepository paqueteRepository, VehiculoRepository vehiculoRepository){
		return args -> {

			// 1. **Definir la lista de datos específicos**
			List<DatosUsuario> datosIniciales = List.of(
					new DatosUsuario("Italo Jeampierre", "Carlos Roque", "italocarlosroque@gmail.com", "70757085", "970818053", TipoDocumentoEnum.DNI),
					new DatosUsuario("Edgar Rise", "Carlos Vilca", "edgar1020@gmail.com", "1613207658", "994205861", TipoDocumentoEnum.PASAPORTE),
					new DatosUsuario("Maria", "Perez Gomez", "mariapg@gmail.com", "45612378", "987654321", TipoDocumentoEnum.DNI),
					new DatosUsuario("Juan", "Lopez Sanchez", "juansl@gmail.com", "987456321", "912345678", TipoDocumentoEnum.PASAPORTE),
					new DatosUsuario("Pepito", "Tomas Feroloco", "peptio@mail.com", "987456300", "987654147", TipoDocumentoEnum.PASAPORTE),
					//new DatosUsuario("Litzy Shannen", "Cervantes Galvan", "lizty@gmail.com", "917456300", "987654142", TipoDocumentoEnum.DNI),
					new DatosUsuario("Geraldin", "Porraz Milagroso", "geraldin@gmail.com", "932547851", "987654177", TipoDocumentoEnum. CARNET_EXTRANJERIA)

			);

			List<Usuario> usuarios = new ArrayList<>();

			// 2. **Iterar sobre la lista de datos iniciales**
			for (DatosUsuario datos : datosIniciales) {

				Usuario usuario = Usuario.builder()
						// Asignar los valores del objeto DatosUsuario
						.nombre(datos.nombre())
						.apellido(datos.apellido())
						.email(datos.email())
						.tipoDocumento(datos.tipoDocumento())
						.numeroDocumento(datos.numeroDocumento())
						.telefono(datos.telefono())

						// Campos estáticos (o lógicos) que son iguales para todos
						.fechaRegistro(LocalDateTime.now())
						.activo(true)
						.build();

				usuarios.add(usuario);
			}

			// 3. Guardar todos los usuarios a la vez
		usuarioRepository.saveAll(usuarios);

//			System.out.println("✅ Se cargaron " + usuarios.size() + " usuarios iniciales.");

			/*
			Usuario usuario=Usuario.builder()
					.nombre("italo jeampierre")
					.apellido("carlos roque")
					.email("italocarlos@gmail.com")
					.tipoDocumento(TipoDocumentoEnum.DNI)
					.numeroDocumento("70757085")
					.telefono("970818053")
					.fechaRegistro(LocalDateTime.now())
					.estado(EstadoClienteEnum.ACTIVO)
					.activo(true)
					.build();

			Usuario usuario2=Usuario.builder()
					.nombre("edgar rise")
					.apellido("carlos vilca")
					.email("edgar1020@gmail.com")
					.tipoDocumento(TipoDocumentoEnum.PASAPORTE)
					.numeroDocumento("1613207658")
					.telefono("994205861")
					.fechaRegistro(LocalDateTime.now())
					.estado(EstadoClienteEnum.ACTIVO)
					.activo(true)
					.build();

			usuarioRepository.saveAll(List.of(usuario,usuario2));
			 */
			Paquete paquete=Paquete.builder()
					.nombre("PAQUETE + SEGURO ")
					.descripcion("Paquete incluye 10 horas de clase + examen medico")
					.duracionMinutos(240)
					.precioTotal(BigDecimal.valueOf(800.00))
					.activo(true)
					.build();

			Paquete paquete2=Paquete.builder()
					.nombre("CORPORATIVO")
					.descripcion("Paquete incluye 20 horas")
					.duracionMinutos(140)
					.precioTotal(BigDecimal.valueOf(500.00))
					.activo(true)
					.build();
			paqueteRepository.saveAll(List.of(paquete,paquete2));

			Vehiculo vehiculo1=Vehiculo.builder()
					.placa("ABC-456")
					.marca("Audi")
					.modelo("Q5 Sportback")
					.tipoTransmision(TipoTransmisionEnum.AUTOMATICO)
					.estado(EstadoVehiculosEnum.DISPONIBLE)
					.activo(true)
					.build();

			Vehiculo vehiculo2=Vehiculo.builder()
					.placa("CXG-328")
					.marca("BMW")
					.modelo("Serie 5")
					.tipoTransmision(TipoTransmisionEnum.MANUAL)
					.estado(EstadoVehiculosEnum.DISPONIBLE)
					.activo(true)
					.build();

			Vehiculo vehiculo3=Vehiculo.builder()
					.placa("S2R-571")
					.marca("CS15  ")
					.modelo("Changan")
					.tipoTransmision(TipoTransmisionEnum.MANUAL)
					.estado(EstadoVehiculosEnum.DISPONIBLE)
					.activo(true)
					.build();

			Vehiculo vehiculo4=Vehiculo.builder()
					.placa("AXC-632")
					.marca("HUAWEY BIVIO")
					.modelo("HOLANDES")
					.tipoTransmision(TipoTransmisionEnum.MANUAL)
					.estado(EstadoVehiculosEnum.DISPONIBLE)
					.activo(true)
					.build();

			Vehiculo vehiculo5=Vehiculo.builder()
					.placa("D7W-980")
					.marca("Chevrolet")
					.modelo("Equinox")
					.tipoTransmision(TipoTransmisionEnum.MANUAL)
					.estado(EstadoVehiculosEnum.DISPONIBLE)
					.activo(true)
					.build();

			vehiculoRepository.saveAll(List.of(vehiculo1,vehiculo2,vehiculo3,vehiculo4,vehiculo5));
		};

	}
}
