package pages

import data.GameData
import models.UserSession
import kotlinx.html.*
import kotlinx.html.stream.*

fun mainPage(session: UserSession): String {

    // ── Экранирование для JSON-строк ──────────────────────────────────────
    fun String.je() = replace("\\", "\\\\").replace("\"", "\\\"")

    val soundNames = linkedMapOf(
        "P"  to "Звук «Р»",
        "Sh" to "Звук «Ш»",
        "L"  to "Звук «Л»",
        "X"  to "Звук «Х»"
    )

    // ── Строим поисковой индекс ───────────────────────────────────────────
    val items = mutableListOf<String>()

    // Разделы
    listOf(
        "Постановка звуков"                          to "section-sounds",
        "Развитие фонематического слуха"             to "section-phonemic",
        "Формирование лексико-грамматического строя" to "section-lexical",
        "Коррекция связной речи"                     to "section-speech",
        "Коррекция темпа и ритма речи"               to "section-tempo"
    ).forEach { (title, anchor) ->
        items += """{"type":"section","title":"${title.je()}","anchor":"$anchor"}"""
    }

    // Подразделы (отдельные звуки)
    soundNames.forEach { (key, name) ->
        items += """{"type":"sub","title":"${name.je()}","url":"/sound/$key","section":"Постановка звуков"}"""
    }

    // Задания из GameData
    GameData.exercises.forEach { (soundKey, exercises) ->
        val soundName = soundNames[soundKey] ?: return@forEach
        exercises.forEachIndexed { i, (title, _) ->
            items += """{"type":"task","title":"${title.je()}","url":"/sound/$soundKey#exercise-$soundKey-$i","section":"${soundName.je()}"}"""
        }
    }

    val searchIndexJson = items.joinToString(",", "[", "]")

    // ── CSS ───────────────────────────────────────────────────────────────
    val styles = """
        .section-header { display:flex; align-items:center; gap:12px; margin-bottom:20px; }
        .section-icon { width:40px; height:40px; border-radius:10px; display:flex; align-items:center; justify-content:center; font-size:20px; flex-shrink:0; }
        .section-icon-green  { background:#e8f5e9; }
        .section-icon-blue   { background:#e3f2fd; }
        .section-icon-purple { background:#f3e5f5; }
        .section-icon-orange { background:#fff3e0; }
        .section-icon-teal   { background:#e0f2f1; }
        .section-meta h3 { margin:0 0 4px; font-size:17px; color:#333; }
        .section-meta p  { margin:0; font-size:13px; color:#888; }
        .sound-grid { display:grid; grid-template-columns:repeat(auto-fill, minmax(110px,1fr)); gap:10px; }
        .sound-btn { display:block; padding:14px 10px; background:#4CAF50; color:white; text-decoration:none; border-radius:8px; font-size:16px; font-weight:bold; text-align:center; transition:background .2s,transform .1s; }
        .sound-btn:hover { background:#388E3C; transform:translateY(-2px); }
        .coming-soon-block { display:flex; align-items:center; gap:14px; background:#fafafa; border:1px dashed #ddd; border-radius:8px; padding:18px 20px; color:#aaa; font-size:14px; }
        .coming-soon-block span { font-size:22px; }
        /* поиск */
        #search-wrapper { position:relative; margin-bottom:24px; }
        .sr-input-wrap { position:relative; }
        .sr-icon { position:absolute; left:12px; top:50%; transform:translateY(-50%); color:#aaa; pointer-events:none; font-size:16px; }
        #search-input { padding-left:40px !important; font-size:15px; }
        #search-results { display:none; position:absolute; top:calc(100% + 4px); left:0; right:0; background:white; border-radius:8px; box-shadow:0 8px 24px rgba(0,0,0,.15); border:1px solid #e0e0e0; z-index:200; max-height:320px; overflow-y:auto; }
        .sr-item { display:flex; align-items:center; gap:10px; padding:10px 14px; cursor:pointer; border-bottom:1px solid #f5f5f5; }
        .sr-item:last-child { border-bottom:none; }
        .sr-item:hover, .sr-item.sr-focused { background:#f5f5f5; }
        .sr-badge { padding:2px 8px; border-radius:12px; font-size:11px; white-space:nowrap; flex-shrink:0; }
        .sr-badge-section { background:#e8f5e9; color:#2e7d32; }
        .sr-badge-sub     { background:#e3f2fd; color:#1565c0; }
        .sr-badge-task    { background:#fff3e0; color:#e65100; }
        .sr-title  { font-size:14px; color:#333; flex:1; }
        .sr-hint   { font-size:12px; color:#bbb; white-space:nowrap; }
        .sr-empty  { padding:16px 14px; color:#888; font-size:14px; }
        /* подсветка раздела после перехода */
        .section-highlight { animation:sectionPulse 1.6s ease-out; }
        @keyframes sectionPulse {
            0%   { box-shadow:0 0 0 4px rgba(76,175,80,.45), 0 2px 10px rgba(0,0,0,.08); background:#f1f8e9; }
            100% { box-shadow:0 2px 10px rgba(0,0,0,.08); background:white; }
        }
    """

    // ── JavaScript ────────────────────────────────────────────────────────
    val js = """
(function(){
    var index = $searchIndexJson;
    var input   = document.getElementById('search-input');
    var results = document.getElementById('search-results');
    var focused = -1;
    var matches = [];

    input.addEventListener('input', function(){
        var q = this.value.trim().toLowerCase();
        focused = -1;
        if(q.length < 1){ results.style.display='none'; matches=[]; return; }

        matches = index.filter(function(it){ return it.title.toLowerCase().indexOf(q) !== -1; }).slice(0,8);

        if(!matches.length){
            results.innerHTML = '<div class="sr-empty">Ничего не найдено</div>';
            results.style.display = 'block';
            return;
        }

        var labels = { section:['🗂','Раздел','sr-badge-section'], sub:['📖','Подраздел','sr-badge-sub'], task:['📝','Задание','sr-badge-task'] };
        results.innerHTML = matches.map(function(it,i){
            var lb = labels[it.type] || ['📄','',''];
            var hint = it.section ? '<span class="sr-hint">' + it.section + '</span>' : '';
            return '<div class="sr-item" onclick="srSelect(' + i + ')">' +
                '<span class="sr-badge ' + lb[2] + '">' + lb[0] + ' ' + lb[1] + '</span>' +
                '<span class="sr-title">' + it.title + '</span>' + hint +
                '</div>';
        }).join('');
        results.style.display = 'block';
    });

    window.srSelect = function(i){
        var it = matches[i];
        if(!it) return;
        results.style.display = 'none';
        input.value = '';
        focused = -1;

        if(it.type === 'section'){
            var el = document.getElementById(it.anchor);
            if(!el) return;
            el.scrollIntoView({ behavior:'smooth', block:'center' });
            el.classList.remove('section-highlight');
            void el.offsetWidth;
            el.classList.add('section-highlight');
        } else {
            window.location.href = it.url;
        }
    };

    input.addEventListener('keydown', function(e){
        var items = results.querySelectorAll('.sr-item');
        if(e.key === 'ArrowDown'){
            e.preventDefault();
            focused = Math.min(focused+1, items.length-1);
            setFocus(items);
        } else if(e.key === 'ArrowUp'){
            e.preventDefault();
            focused = Math.max(focused-1, 0);
            setFocus(items);
        } else if(e.key === 'Enter'){
            e.preventDefault();
            srSelect(focused >= 0 ? focused : 0);
        } else if(e.key === 'Escape'){
            results.style.display = 'none';
            focused = -1;
            input.blur();
        }
    });

    function setFocus(items){
        items.forEach(function(el,i){ el.classList.toggle('sr-focused', i===focused); });
        if(focused >= 0) items[focused].scrollIntoView({ block:'nearest' });
    }

    document.addEventListener('click', function(e){
        if(!e.target.closest('#search-wrapper')){ results.style.display='none'; focused=-1; }
    });
})();
    """

    return createHTML().html {
        head {
            title { +"Библиотека упражнений — Логопедический помощник" }
            style { unsafe { +commonStyles(); +styles } }
        }
        body {
            navbar(session)
            div("container") {

                div {
                    attributes["style"] = "display:flex; justify-content:space-between; align-items:center; margin-bottom:20px;"
                    h2 { attributes["style"] = "margin:0; color:#333;"; +"Библиотека упражнений" }
                    a("/dashboard") { attributes["class"] = "btn btn-secondary"; +"← Назад" }
                }

                // ── Поиск ─────────────────────────────────────────────────
                div { id = "search-wrapper"
                    div("card") {
                        attributes["style"] = "padding:16px 20px; overflow:visible;"
                        div("sr-input-wrap") {
                            span("sr-icon") { +"🔍" }
                            input(type = InputType.text) {
                                id = "search-input"
                                attributes["class"] = "form-control"
                                attributes["placeholder"] = "Поиск по библиотеке — разделы, звуки, задания..."
                                attributes["autocomplete"] = "off"
                            }
                            div { id = "search-results" }
                        }
                    }
                }

                // ── 1. Постановка звуков ──────────────────────────────────
                div("card") {
                    id = "section-sounds"
                    div("section-header") {
                        div("section-icon section-icon-green") { +"🔤" }
                        div("section-meta") {
                            h3 { +"Постановка звуков" }
                            p { +"Упражнения по автоматизации и дифференциации звуков" }
                        }
                    }
                    div("sound-grid") {
                        a("/sound/P",  classes = "sound-btn") { +"Р" }
                        a("/sound/Sh", classes = "sound-btn") { +"Ш" }
                        a("/sound/L",  classes = "sound-btn") { +"Л" }
                        a("/sound/X",  classes = "sound-btn") { +"Х" }
                    }
                }

                // ── 2. Развитие фонематического слуха ────────────────────
                div("card") {
                    id = "section-phonemic"
                    div("section-header") {
                        div("section-icon section-icon-blue") { +"👂" }
                        div("section-meta") {
                            h3 { +"Развитие фонематического слуха" }
                            p { +"Различение звуков, слогов и слов на слух" }
                        }
                    }
                    div("coming-soon-block") {
                        span { +"🚧" }
                        +"Раздел в разработке — скоро добавим упражнения"
                    }
                }

                // ── 3. Лексико-грамматический строй ──────────────────────
                div("card") {
                    id = "section-lexical"
                    div("section-header") {
                        div("section-icon section-icon-purple") { +"📚" }
                        div("section-meta") {
                            h3 { +"Формирование лексико-грамматического строя" }
                            p { +"Словарный запас, словоизменение, словообразование" }
                        }
                    }
                    div("coming-soon-block") {
                        span { +"🚧" }
                        +"Раздел в разработке — скоро добавим упражнения"
                    }
                }

                // ── 4. Коррекция связной речи ─────────────────────────────
                div("card") {
                    id = "section-speech"
                    div("section-header") {
                        div("section-icon section-icon-orange") { +"💬" }
                        div("section-meta") {
                            h3 { +"Коррекция связной речи" }
                            p { +"Пересказ, составление рассказов, диалог" }
                        }
                    }
                    div("coming-soon-block") {
                        span { +"🚧" }
                        +"Раздел в разработке — скоро добавим упражнения"
                    }
                }

                // ── 5. Коррекция темпа и ритма речи ──────────────────────
                div("card") {
                    id = "section-tempo"
                    div("section-header") {
                        div("section-icon section-icon-teal") { +"🎵" }
                        div("section-meta") {
                            h3 { +"Коррекция темпа и ритма речи" }
                            p { +"Заикание, тахилалия, брадилалия, ритмизация" }
                        }
                    }
                    div("coming-soon-block") {
                        span { +"🚧" }
                        +"Раздел в разработке — скоро добавим упражнения"
                    }
                }
            }

            script { unsafe { +js } }
        }
    }
}
