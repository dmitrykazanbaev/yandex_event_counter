# yandex_event_counter
Некоторые подробности реализации:
* Секунда - дискретная единица времени. Миллисекунды не учитываются. Позволяет хранить в мапе по ключу-дате количество вызовов.
* Счетчик вызовов реализован как синхронизированный синглтон с ленивой загрузкой
* Для исключения возможного оверфлоу числовых типов используется BigInteger
* Так как нет методов, возвращающих записи старше 24 часов, есть таймер-таска, которая каждый день чистит мапу от устаревших записей, что предотвращает бесконечное увеличение мапы
