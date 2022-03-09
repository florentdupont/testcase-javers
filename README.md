

Comme la structure du JSON est stockée, en cas d'évolution, il risque d'y avoir des soucis.
Il s'agit d'un probléme général : dès lors que l'on met en place une notion d'historique, il y'aura forcément des soucis
lors des comparaisons si le modèle vient à changer !


## Value Object
Dans le cas du Tiers, il y'a surement un soucis de modélisation : l'Adresse devrait être une ValueObject.
Normalement une valueObject se traduit par un Embeddable -> mais ca suppose d'intégrer tous les colonnes
de l'entité Adress au sein de la table Tiers, ce qui n'est pas forcément terrible non plus.

Dans les besoins de THibaud, on veut pouvoir faire en sorte que : 
-> Une modification de l'adresse soit indiquée en tant que modification du Tiers...



Il est préférable de bien utiliser des Use @TypeName annotation for Entities, 
it gives you freedom of class names refactoring. Et en plus, ca prend moins de place.


https://stackoverflow.com/questions/57972894/javers-production-best-practices

There is one performance hint -- keep Javers data small. You should control the number of Snapshots persisted to 
JaversRepository.

Applications should track changes only in important data that are entered by users. 
You can call it core-domain data or business-relevant data. 
All technical data, data imported from other systems and generated data should be ignored. 
There are various ways of ignoring things in Javers.

At the end of the day, when you show a change log to your users, it should look like a concise, human-readable story, for example:

https://stackoverflow.com/questions/57391748/how-to-clean-up-snapshots-and-commits-after-a-period-of-time-in-javers



Redémarrage à chaud : https://stackoverflow.com/questions/33349456/how-to-make-auto-reload-with-spring-boot-on-idea-intellij
