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

+ Area -
+ Intersección -
+ Segmento -
+ Step -
+ Truck -


Version Control
===============
El proyecto está formado por varias ramas. 
Las más importantes son la master y la develop.

+ master - Es la rama madre de la que parten las demás y solo se utilizará para unir las versiones estables del proyecto.

+ develop - Es la rama más utilizada y de ella salen ramas secundarias con el objetivo de cumplir una funcionalidad. Por ejemplo ComunicaciónJSON es una rama que se crea con el objetivo de cambiar el proyecto inicial para que las comunicaciones entre agentes se realice utilizando JSON.

Además de esto, se insta a utilizar descripciones de commit extensas y explicativas. En el caso de ocurrir conflictos, es beneficioso poner las correcciones que se han realizado en la descripción del commit además de dejar constancia que ha habido un conflicto.

#Dependencias

Las principales dependencias del proyecto son JADE (http://jade.tilab.com/download/jade/) y JGraphT (http://jgrapht.org/). Para poder ejecutar el proyecto es necesario añadir las librerias de ambas dependencias.

#ToDo
Tenemos que hacer las siguientes pruebas:

Utilizando 2 areas:

Test 1 - 4 aparcados bien y 1 ilegales

Test 2 - 2 aparcados bien y 3 ilegales 

Test 3 - Todos aparcados bien

Test 4 - Todos aparcados ilegales

Tenemos que sacar un fichero de configuración de Jose 
o algo así y hemos de sacar un log.
