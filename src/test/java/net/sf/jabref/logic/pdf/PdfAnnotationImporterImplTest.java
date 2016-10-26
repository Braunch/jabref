package net.sf.jabref.logic.pdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import net.sf.jabref.logic.importer.ImportFormatPreferences;
import net.sf.jabref.logic.importer.ParserResult;
import net.sf.jabref.logic.importer.fileformat.BibtexParser;
import net.sf.jabref.model.database.BibDatabase;
import net.sf.jabref.model.database.BibDatabaseContext;
import net.sf.jabref.model.pdf.FileAnnotation;
import net.sf.jabref.preferences.JabRefPreferences;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.fdf.FDFAnnotationText;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PdfAnnotationImporterImplTest {

    private PdfAnnotationImporterImpl importer;
    private File testPdf;
    private BibDatabase testDB;
    private ImportFormatPreferences importFormatPreferences;
    private final static int TEST_ANNOTATION_1 = 88;


    @Before
    public void setUp(){
        this.importer = new PdfAnnotationImporterImpl();
        importFormatPreferences = JabRefPreferences.getInstance().getImportFormatPreferences();
        testDB = null;
    }

    @Test
    public void importAnnotationsFromCorrectPdf() throws Exception {

        FileInputStream stream = new FileInputStream(
                PdfAnnotationImporterImplTest.class.getResource("annotationTabDB.bib").getPath());

        InputStreamReader inputStreamReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        ParserResult result = BibtexParser.parse(inputStreamReader, importFormatPreferences);
        testDB = result.getDatabase();

        BibDatabaseContext context = new BibDatabaseContext(testDB);
        List<FileAnnotation> annotationList = importer.importAnnotations(
                PdfAnnotationImporterImplTest.class.getResource("thesis-example.pdf").getPath(), context);
        FileAnnotation expectedAnnotation = new FileAnnotation("unknown",
                "Tobias",
                "D:20161026171902+02'00'",
                22,
                "Bubble with No Highlight",
                FDFAnnotationText.SUBTYPE);

        assertEquals( expectedAnnotation.getAuthor(), annotationList.get(TEST_ANNOTATION_1).getAuthor());
        assertEquals( expectedAnnotation.getDate(),annotationList.get(TEST_ANNOTATION_1).getDate());
        assertEquals( expectedAnnotation.getPage(), annotationList.get(TEST_ANNOTATION_1).getPage());
        assertEquals( expectedAnnotation.getAnnotationType(), annotationList.get(TEST_ANNOTATION_1).getAnnotationType());
    }

    @Test
    public void importCorrectPdfFile() throws Exception {
        testPdf = new File(PdfAnnotationImporterImplTest.class.getResource("thesis-example.pdf").toURI());
        PDDocument expectedFile = PDDocument.load(PdfAnnotationImporterImplTest.class.getResource("thesis-example.pdf"));

        PDDocument actualFile = importer.importPdfFile(testPdf.getAbsolutePath());

        assertEquals(expectedFile.getDocumentId(), actualFile.getDocumentId());
    }

    @Test
    public void importFileWithOtherEnding() throws Exception {
        testPdf = new File(String.valueOf(PdfAnnotationImporterImplTest.class.getResource("thesis-example.txt")));
        PDDocument actualFile = importer.importPdfFile(testPdf.getAbsolutePath());

        assertNull(actualFile);
    }

}