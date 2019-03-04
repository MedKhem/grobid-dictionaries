/**
 *  Javascript functions for the front end.
 *
 *  Author: Patrice Lopez
 */

jQuery.fn.prettify = function () {
    this.html(prettyPrintOne(this.html(), 'xml'));
};
var xmlToDownload;
var grobid = (function ($) {



    $(document).ready(function () {


        $("#subTitle").html("About");
        $("#divAbout").show();
        $("#divAdmin").hide();
        $("#divRestI").hide();
        $("#divRestII").hide();
        $("#divDoc").hide();
        $('#consolidateBlock').show();
        $("#btn_download").hide();
        $("#btn_downloadBib").hide();
        $('#refinedModels').hide();

        createInputFile();
        createInputFileBib();
        setBaseUrl('processLexicalEntry');

        // $('#selectedDictionaryService').change(function () {
        //     processDictionaryChange();
        //     return true;
        // });

        // $('#checkOptimise').change(function () {
        //     processDictionaryChange();
        //     return true;
        // });


        $('#gbdForm').ajaxForm({
            beforeSubmit: ShowRequest,
            success: SubmitSuccesful,
            error: AjaxError,
            dataType: "text"
        });

        $('#gbdBibForm').ajaxForm({
            beforeSubmit: ShowRequestBib,
            success: SubmitBibSuccesful,
            error: AjaxErrorBib,
            dataType: "text"
        });


        // bind downloadDictionary buttons with downloadDictionary methods
        $('#btn_download').bind('click', downloadDictionary);
        $("#btn_download").hide();

        $('#btn_download_lemmas').bind('click', downloadLemma);
        $("#btn_download_lemmas").hide();


        $('#btn_downloadBib').bind('click', downloadBibliography());
        $("#btn_downloadBib").hide();

        $('#btn_block_1').bind('click', downloadVisibilty);
        $('#btn_block_Bib').bind('click', downloadVisibiltyBib);
        $('#adminForm').attr("action", $(location).attr('href') + "allProperties");
        $('#TabAdminProps').hide();
        $('#adminForm').ajaxForm({
            beforeSubmit: adminShowRequest,
            success: adminSubmitSuccesful,
            error: adminAjaxError,
            dataType: "text"
        });

        $("#about").click(function () {
            $("#about").attr('class', 'section-active');
            $("#rest").attr('class', 'section-not-active');
            $("#restBib").attr('class', 'section-not-active');
            $("#admin").attr('class', 'section-not-active');
            $("#doc").attr('class', 'section-not-active');
            $("#demo").attr('class', 'section-not-active');

            $("#subTitle").html("About");
            $("#subTitle").show();

            $("#divAbout").show();
            $("#divRestI").hide();
            $("#divRestII").hide();
            $("#divAdmin").hide();
            $("#divDoc").hide();
            $("#divDemo").hide();
            return false;
        });
        $("#rest").click(function () {
            $("#rest").attr('class', 'section-active');
            $("#restBib").attr('class', 'section-not-active');
            $("#doc").attr('class', 'section-not-active');
            $("#about").attr('class', 'section-not-active');
            $("#admin").attr('class', 'section-not-active');
            $("#demo").attr('class', 'section-not-active');

            $("#subTitle").hide();
            //$("#subTitle").show();
            processDictionaryChange();

            $("#divRestI").show();
            $("#divRestII").hide();
            $("#divAbout").hide();
            $("#divDoc").hide();
            $("#divAdmin").hide();
            $("#divDemo").hide();
            return false;
        });
        $("#restBib").click(function () {
            $("#restBib").attr('class', 'section-active');
            $("#rest").attr('class', 'section-not-active');
            $("#doc").attr('class', 'section-not-active');
            $("#about").attr('class', 'section-not-active');
            $("#admin").attr('class', 'section-not-active');
            $("#demo").attr('class', 'section-not-active');

            $("#subTitle").hide();
            //$("#subTitle").show();
            processBibliographyChange();

            $("#divRestI").hide();
            $("#divRestII").show();
            $("#divAbout").hide();
            $("#divDoc").hide();
            $("#divAdmin").hide();
            $("#divDemo").hide();
            return false;
        });
        $("#admin").click(function () {
            $("#admin").attr('class', 'section-active');
            $("#doc").attr('class', 'section-not-active');
            $("#about").attr('class', 'section-not-active');
            $("#rest").attr('class', 'section-not-active');
            $("#restBib").attr('class', 'section-not-active');
            $("#demo").attr('class', 'section-not-active');

            $("#subTitle").html("Admin");
            $("#subTitle").show();
            setBaseUrl('admin');

            $("#divRestI").hide();
            $("#divRestII").hide();
            $("#divAbout").hide();
            $("#divDoc").hide();
            $("#divAdmin").show();
            $("#divDemo").hide();
            return false;
        });
        $("#doc").click(function () {
            $("#doc").attr('class', 'section-active');
            $("#rest").attr('class', 'section-not-active');
            $("#restBib").attr('class', 'section-not-active');
            $("#about").attr('class', 'section-not-active');
            $("#admin").attr('class', 'section-not-active');
            $("#demo").attr('class', 'section-not-active');

            $("#subTitle").html("Doc");
            $("#subTitle").show();

            $("#divDoc").show();
            $("#divAbout").hide();
            $("#divRestI").hide();
            $("#divRestII").hide();
            $("#divAdmin").hide();
            $("#divDemo").hide();
            return false;
        });
        $("#demo").click(function () {
            $("#demo").attr('class', 'section-active');
            $("#rest").attr('class', 'section-not-active');
            $("#restBib").attr('class', 'section-not-active');
            $("#about").attr('class', 'section-not-active');
            $("#admin").attr('class', 'section-not-active');
            $("#doc").attr('class', 'section-not-active');

            $("#subTitle").html("Demo");
            $("#subTitle").show();

            $("#divDemo").show();
            $("#divDoc").hide();
            $("#divAbout").hide();
            $("#divRestI").hide();
            $("#divRestII").hide();
            $("#divAdmin").hide();
            return false;
        });

        $('#selectedDictionaryService').change(function () {
            processDictionaryChange();
            return true;
        });
        $('#selectedBibliographyService').change(function () {
            processBibliographyChange();
            return true;
        });
        $('#selectedFormService').change(function () {
            processDictionaryChange();
            return true;
        });
        $('#selectedSenseService').change(function () {
            processDictionaryChange();
            return true;
        });
        $('#selectedEtymService').change(function () {
            processDictionaryChange();
            return true;
        });
        $('#selectedReService').change(function () {
            processDictionaryChange();
            return true;
        });
        $('#selectedXrService').change(function () {
            processDictionaryChange();
            return true;
        });
        $('#selectedSubEntryService').change(function () {
            processDictionaryChange();
            return true;
        });
        $('#selectedNoteService').change(function () {
            processDictionaryChange();
            return true;
        });

    });

    function ShowRequest(formData, jqForm, options){
        //console.log(formData.value);
        var queryString = $.param(formData);
        $('#requestResult').html('<font color="grey">Requesting server...</font>');
        return true;
    }
    function ShowRequestBib(formData, jqForm, options){
        //console.log(formData.value);
        var queryString = $.param(formData);
        $('#requestResultBib').html('<font color="grey">Requesting server...</font>');
        return true;
    }

    function AjaxError(jqXHR, textStatus, errorThrown) {
        $('#requestResult').html("<font color='red'>Error encountered while requesting the server.<br/>" + jqXHR.responseText + "</font>");
        responseJson = null;
    }
    function AjaxErrorBib(jqXHR, textStatus, errorThrown) {
        $('#requestResultBib').html("<font color='red'>Error encountered while requesting the server.<br/>" + jqXHR.responseText + "</font>");
        responseJson = null;
    }

    function htmll(s) {
        return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    }

    function SubmitSuccesful(responseText, statusText, xhr) {
        var selected = $('#selectedDictionaryService option:selected').attr('value');
        var display = "<pre class='prettyprint lang-xml' id='xmlCode'>";
        var testStr = vkbeautify.xml(responseText);
        // console.log(responseText);
        // console.log(testStr);
        xmlToDownload = responseText;
        display += htmll(testStr);

        display += "</pre>";
        $('#requestResult').html(display);
        window.prettyPrint && prettyPrint();


        $('#requestResult').show();
        $("#btn_download").show();
        var selectedDictionaryForm = $('#selectedFormService option:selected').attr('value');
        console.log(selectedDictionaryForm);
        if (selectedDictionaryForm == 'form' || selectedDictionaryForm == 'customisedForm'){
            $('#btn_download_lemmas').show();
        }else{
            $('#btn_download_lemmas').hide();
        }
    }

    function SubmitBibSuccesful(responseText, statusText, xhr) {
        var selected = $('#selectedBibliographyService option:selected').attr('value');
        var display = "<pre class='prettyprint lang-xml' id='xmlCode'>";
        var testStr = vkbeautify.xml(responseText);
        // console.log(responseText);
        // console.log(testStr);
        xmlToDownload = responseText;
        display += htmll(testStr);

        display += "</pre>";
        $('#requestResultBib').html(display);
        window.prettyPrint && prettyPrint();
        $('#requestResultBib').show();
        $("#btn_downloadBib").show();
    }

    $(document).ready(function () {
        $(document).on('shown', '#xmlCode', function (event) {
            prettyPrint();
        });
    });





    function createInputTextArea(nameInput) {
        //$('#label').html('&nbsp;'); 
        $('#fileInputDiv').hide();
        //$('#input').remove();

        //$('#field').html('<table><tr><td><textarea class="span7" rows="5" id="input" name="'+nameInput+'" /></td>'+
        //"<td><span style='padding-left:20px;'>&nbsp;</span></td></tr></table>");
        $('#textInputArea').attr('name', nameInput);
        $('#textInputDiv').show();

        $('#gbdForm').attr('enctype', '');
        $('#gbdForm').attr('method', 'post');
    }
    
    


    /** admin functions */

    var selectedAdmKey = "", selectedAdmValue, selectedAdmType;

    function adminShowRequest(formData, jqForm, options) {
        $('#TabAdminProps').show();
        $('#admMessage').html('<font color="grey">Requesting server...</font>');
        return true;
    }

    function adminAjaxError() {
        $('#admMessage').html("<font color='red'>Autentication error.</font>");
    }

    function adminSubmitSuccesful(responseText, statusText) {
        $('#admMessage').html("<font color='green'>Welcome to the admin console.</font>");
        parseXml(responseText);
        rowEvent();
    }

    function parseXml(xml) {
        var out = "<pre><table class='table-striped table-hover'><thead><tr align='left'><th>Property</th><th align='left'>value</th></tr></thead>";
        $(xml).find("property").each(function () {
            var dsipKey = $(this).find("key").text();
            var key = dsipKey.split('.').join('-');
            var value = $(this).find("value").text();
            var type = $(this).find("type").text();
            out += "<tr class='admRow' id='" + key + "'><td><input type='hidden' value='" + type + "'/>" + dsipKey + "</td><td><div>" + value + "</div></td></tr>";
        });
        out += "</table></pre>";
        $('#TabAdminProps').html(out);
    }

    function rowEvent() {
        $('.admRow').click(function () {
            $("#" + selectedAdmKey).find("div").html($("#val" + selectedAdmKey).attr("value"));
            selectedAdmKey = $(this).attr("id");
            selectedAdmValue = $(this).find("div").text();
            selectedAdmType = $(this).find("input").attr("value");
            $(this).find("div").html("<input type='text' id='val" + selectedAdmKey + "' size='80' value='" + selectedAdmValue + "' class='input-xxlarge'/>");
            $("#val" + selectedAdmKey).focus();
        });

        $('.admRow').keypress(function (event) {
            var keycode = (event.keyCode ? event.keyCode : event.which);
            selectedAdmKey = $(this).attr("id");
            // Enter key
            if (keycode == '13') {
                var newVal = $("#val" + selectedAdmKey).val();
                $("#" + selectedAdmKey).find("div").html(newVal);
                selectedAdmValue = newVal;
                selectedAdmType = $(this).find("input").attr("value");
                generateXmlRequest();
            }
            // Escape key
            if (keycode == '27') {
                $("#" + selectedAdmKey).find("div").html(selectedAdmValue);
            }
        });
    }

    function generateXmlRequest() {
        var xmlReq = "<changeProperty><password>" + $('#admPwd').val() + "</password>";
        xmlReq += "<property><key>" + selectedAdmKey.split('-').join('.') + "</key><value>" + selectedAdmValue + "</value><type>" + selectedAdmType + "</type></property></changeProperty>";
        if ("org.grobid.service.admin.pw" == selectedAdmKey.split('-').join('.')) {
            $('#admPwd').attr('value', selectedAdmValue);
        }
        $.ajax({
            type: 'POST',
            url: $(location).attr('href') + "changePropertyValue",
            data: {xml: xmlReq},
            success: changePropertySuccesful,
            error: changePropertyError
        });
    }

    function changePropertySuccesful(responseText, statusText) {
        $("#" + selectedAdmKey).find("div").html(responseText);
        $('#admMessage').html("<font color='green'>Property " + selectedAdmKey.split('-').join('.') + " updated with success</font>");
    }

    function changePropertyError() {
        $('#admMessage').html("<font color='red'>An error occured while updating property" + selectedAdmKey.split('-').join('.') + "</font>");
    }

})(jQuery);

function processDictionaryChange()  {
    var selectedMacroLevel = $('#selectedDictionaryService option:selected').attr('value');
    // var checked = $('#checkOptimise').is(':checked');


   if (selectedMacroLevel == 'processDictionarySegmentation') {
        // if(checked == true){
        //    //Nothing to optimise yet
        // }
        // else {
            createInputFile(selectedMacroLevel);
       $('#refinedModels').hide();
            setBaseUrl('processDictionarySegmentation');
        // }
    }
    else if (selectedMacroLevel == 'processDictionaryBodySegmentation') {

            createInputFile(selectedMacroLevel);
       $('#refinedModels').hide();

            setBaseUrl('processDictionaryBodySegmentation');

    }
    else if (selectedMacroLevel == 'processLexicalEntry') {

            createInputFile(selectedMacroLevel);

       $('#refinedModels').hide();
            setBaseUrl('processLexicalEntry');

    }
    else if (selectedMacroLevel == 'processFullDictionary' ) {

       var form = $('#selectedFormService option:selected').attr('value');
       var sense = $('#selectedSenseService option:selected').attr('value');
       var etym = $('#selectedEtymService option:selected').attr('value');
       var re = $('#selectedReService option:selected').attr('value');
       var xr = $('#selectedXrService option:selected').attr('value');
       var subEntry = $('#selectedSubEntryService option:selected').attr('value');
       var note = $('#selectedNoteService option:selected').attr('value');
        // console.log(form.concat('/').concat(sense).concat('/').concat(etym).concat('/').concat(re).concat('/').concat(xr).concat('/').concat(subEntry).concat('/').concat(note).concat('.processFullDictionary'));

        //   createInputFile(selectedMacroLevel);
       $('#refinedModels').show();
           setBaseUrl(form.concat('/').concat(sense).concat('/').concat(etym).concat('/').concat(re).concat('/').concat(xr).concat('/').concat(subEntry).concat('/').concat(note).concat('.processFullDictionary'));



   }

}

function processBibliographyChange()  {
    var selected = $('#selectedBibliographyService option:selected').attr('value');
    // var checked = $('#checkOptimise').is(':checked');


    if (selected == 'processBibliographySegmentation') {
        // if(checked == true){
        //    //Nothing to optimise yet
        // }
        // else {
        createInputFileBib(selected);
        $('#consolidateBlock').show();
        setBaseUrlBib('processBibliographySegmentation');
        // }
    }
    else if (selected == 'processBibliographyBodySegmentation') {

        createInputFileBib(selected);
        $('#consolidateBlock').show();
        setBaseUrlBib('processBibliographyBodySegmentation');

    }
    else if (selected == 'processBibliographyEntry') {

        createInputFileBib(selected);
        $('#consolidateBlock').show();
        setBaseUrlBib('processBibliographyEntry');

    }


}
// or, if you want to encapsulate variables within the plugin
(function($) {
    $.fn.MessageBoxScoped = function(msg) {
        alert(msg);
    };
})(jQuery);

function downloadDictionary() {
    var name = "export";
    if ((document.getElementById("input").files[0].type == 'application/pdf') ||
        (document.getElementById("input").files[0].name.endsWith(".pdf")) ||
        (document.getElementById("input").files[0].name.endsWith(".PDF"))) {
        name = document.getElementById("input").files[0].name;
    }
    var fileName = name + ".tei.xml";
    var a = document.createElement("a");


    var file = new Blob([xmlToDownload], {type: 'application/xml'});
    var fileURL = URL.createObjectURL(file);
    a.href = fileURL;
    a.download = fileName;

    document.body.appendChild(a);

    $(a).ready(function () {
        a.click();
        return true;
    });
}

function downloadLemma() {
    var name = "export";
    if ((document.getElementById("input").files[0].type == 'application/pdf') ||
        (document.getElementById("input").files[0].name.endsWith(".pdf")) ||
        (document.getElementById("input").files[0].name.endsWith(".PDF"))) {
        name = document.getElementById("input").files[0].name;
    }
    var fileName = name + ".txt";
    var a = document.createElement("a");



    var parser, xmlDoc, segmentedBody, entryList;
    parser = new DOMParser();
    xmlDoc = parser.parseFromString(xmlToDownload,"application/xml");

    entryList = xmlDoc.getElementsByTagName("entry");
    console.log(entryList.length.toString());
    // entryList = segmentedBody.getElementsByTagName("entry");
    var lemmas = "";
    var form, i;
    for (i = 0; i< entryList.length; i++) {
        form = entryList[i].getElementsByTagName("form")[0];

        lemmas += form.getElementsByTagName("orth")[0].childNodes[0].nodeValue +" "+ entryList[i].getElementsByTagName("gramGrp")[0].getElementsByTagName("pos")[0].childNodes[0].nodeValue +"\n";
    }
    console.log(lemmas);


    var file = new Blob([lemmas], {type: 'text/plain'});
    var fileURL = URL.createObjectURL(file);
    a.href = fileURL;
    a.download = fileName;

    document.body.appendChild(a);

    $(a).ready(function () {
        a.click();
        return true;
    });
}
function downloadBibliography() {
    // var name = "export";
    // if ((document.getElementById("input").files[1].type == 'application/pdf') ||
    //     (document.getElementById("input").files[1].name.endsWith(".pdf")) ||
    //     (document.getElementById("input").files[1].name.endsWith(".PDF"))) {
    //     name = document.getElementById("input").files[0].name;
    // }
    // var fileName = name + ".tei.xml";
    // var a = document.createElement("a");
    //
    //
    // var file = new Blob([xmlToDownload], {type: 'application/xml'});
    // var fileURL = URL.createObjectURL(file);
    // a.href = fileURL;
    // a.download = fileName;
    //
    // document.body.appendChild(a);
    //
    // $(a).ready(function () {
    //     a.click();
    //     return true;
    // });
}
function defineBaseURL(ext) {
    var baseUrl = null;
    if ($(location).attr('href').indexOf("index.html") != -1)
        baseUrl = $(location).attr('href').replace("index.html", ext);
    else
        baseUrl = $(location).attr('href') + ext;
    return baseUrl;
}

function setBaseUrl(ext) {
    var baseUrl = defineBaseURL(ext);
    $('#gbdForm').attr('action', baseUrl);
}
function setBaseUrlBib(ext) {
    var baseUrl = defineBaseURL(ext);
    $('#gbdBibForm').attr('action', baseUrl);
}

function createInputFile(selected) {
    //$('#label').html('&nbsp;');
    $('#textInputDiv').hide();
    //$('#fileInputDiv').fileupload({uploadtype:'file'});
    //$('#fileInputDiv').fileupload('reset');
    $('#fileInputDiv').show();

    $('#gbdForm').attr('enctype', 'multipart/form-data');
    $('#gbdForm').attr('method', 'post');
}

function createInputFileBib(selected) {
    //$('#label').html('&nbsp;');
    $('#textInputDivBib').hide();
    //$('#fileInputDiv').fileupload({uploadtype:'file'});
    //$('#fileInputDiv').fileupload('reset');
    $('#fileInputDivBib').show();

    $('#gbdBibForm').attr('enctype', 'multipart/form-data');
    $('#gbdBibForm').attr('method', 'post');
}

function downloadVisibilty(){
    $("#btn_download").hide();
}

function downloadVisibiltyBib(){
    $("#btn_downloadBib").hide();
}


