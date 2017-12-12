RoadSimTruck
========
Documentación oficial del proyecto de investigación de la Universitat Jaume I : RoadSimTruck.

Este proyecto parte de otro proyecto diferente, [RoadSimV2V](https://github.com/garcial/RoadSimV2V), por lo que existe código, a priori inutilizado, que era útil para el proyecto origen.

#Descripción conceptual del proyecto

El proyecto tiene como objetivo gestionar los estacionamientos de los vehículos (camiones) 
durante su trayecto. Un camión puede estar en la carretera durante un número específico de kiloómetros antes
de estacionar. Si su trayecto es mayor a este número de kilómetros es necesario que el vehículo estacione en un área
de descanso. RoadSimTruck es un simulador en el que se inicial vehículos con un origen y un destino conocido y se encarga de realizar
el trayecto de la manera óptima, con el mínimo número de paradas.

#Conceptos necesarios

Los siguientes conceptos son necesarios para la compresión del funcionamiento de RoadSimTruck

+ Area - Elemento en el que los camiones pueden realizar un estacionamiento (Desarrollado como una intersección).

+ Intersección - Punto de cambio entre vías. Puede ser una rotonda, una salida de la vía, una entrada en la via, un cambio en el número de cariles o en general
cualquier cambio en las características de la vía.

+ Segmento - Carretera que transcurre entre una intersección de origen y una intersección de destino.

+ Step - División de segmentos en parcelas más reducidas para adecuar la carretera al mapa.

+ Truck - Camión que aparece en un punto concreto de la carretera con una intersección de destino, calcula su intinerario y se dirige al destino 
parando en las Areas que le resulten más beneficiosas para realizar su recorrido. 


#Version Control

El proyecto está formado por varias ramas. 
Las más importantes son la master y la develop.

+ master - Es la rama madre de la que parten las demás y solo se utilizará para unir las versiones estables del proyecto.

+ develop - Es la rama más utilizada y de ella salen ramas secundarias con el objetivo de cumplir una funcionalidad. Por ejemplo ComunicaciónJSON es una rama que se crea con el objetivo de cambiar el proyecto inicial para que las comunicaciones entre agentes se realice utilizando JSON.

Además de esto, se insta a utilizar descripciones de commit extensas y explicativas. En el caso de ocurrir conflictos, es beneficioso poner las correcciones que se han realizado en la descripción del commit además de dejar constancia que ha habido un conflicto.

#Dependencias

Las principales dependencias del proyecto son [Java8](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html) ,[JADE](http://jade.tilab.com/download/jade/) y [JGraphT](http://jgrapht.org/). Para poder ejecutar el proyecto es necesario añadir las librerias de ambas dependencias.

#Ficheros necesarios 

Este sistema funciona a partir de unos ficheros que se generan a partir del repositorio [ImgToJsonGUI](https://github.com/AxBS/ImgToJsonGUI).

Los ficheros que genera este repositorio son los siguientes:

+ red.png o ESCENARIO_SIN_NOMBRE.png - Es la imagen del fondo del programa.

+ segments.json - JSON con la información de los segmentos.

+ intersection.json - JSON con la información de las intersecciones.

+ steps.json - JSON con la información de los steps.

+ prohibitions.json - Este fichero tiene los caminos permitidos del mapa. No se utiliza en este repositorio.

Además de estos ficheros también se necesita el fichero events.csv con la información de los Trucks que van a entrar en el sistema.

#Funcionamiento 

La clase principal del sistema es el fichero main/main.java. Esta clase, a su vez, va creando y lanzando instancias de las
demás clases participantes del proyecto de la siguiente manera:

1.Primero encontramos todos los valores que va a usar la aplicación, como por ejemplo el primer y último
tick de la simulación, así como la longitud de estos.

2.En la ejecución del programa, primero crea una instancia del agente RMA para control de los otros agentes
del sistema.

3.Se crean y lanzan instancias de los containers para Segments y Areas.

4.Cargamos el mapa sobre el que se va a realizar la simulación.

5.Procedemos con el lanzamiento de los agentes principales, el de la interfaz, si es necesario,
el TimeKeeperAgent (encargado de lanzar los ticks del sistema), y los containers para los coches (carContainer)
y para los camiones (truckContainer).

6.Finalmente creamos y lanzamos una instancia del EventManager, el agente encargado de leer el archivo de eventos
y traducirlo en el lanzamiento de los agentes necesarios, tanto de coches como de camiones.

#Comunicaciones

Las comunicaciones básicas del sistema esta en este [documento](https://docs.google.com/document/d/1848YJjbIVC82Ef8d5pPVPDiNrgqNmWWhjIhvYVcVrno/edit)

#Screenshot

Imagen general de RoadSim

![I once had chickens](https://raw.githubusercontent.com/pjimenezmateo/RoadSim/master/screenshot.png)


![RoadSimTruck Image](https://raw.githubusercontent.com/AxBS/RoadSimTruck/blob/develop/src/staticFiles/images/ScreenshotImagenRoadSimTruck.png)

#ToDo

Modificaciones a realizar (posteriormente):

Comportamiento al liberar una plaza en una area:    
    Dinamismo al proyecto, al liberar un area, enviamos un mensaje a los camiones que nos han pedido sitio para avisar que tenemos plaza libre.

Montener un histórico de los vehículos que han pedido plaza en cada area.
    -Este hitórico se mantiene en las areas hasta que los trucks dejan de estar interesados en las plazas (realizan una reserva formal).



Tenemos que hacer las siguientes pruebas:

Utilizando 2 areas:

Test 1 - 4 aparcados bien y 1 ilegales

Test 2 - 2 aparcados bien y 3 ilegales 

Test 3 - Todos aparcados bien

Test 4 - Todos aparcados ilegales


Tenemos que sacar un fichero de configuración de Jose 
o algo así y hemos de sacar un log.

```javascript
function(){
    test
}
```