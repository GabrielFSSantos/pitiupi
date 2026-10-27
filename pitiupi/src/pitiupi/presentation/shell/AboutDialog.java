package pitiupi.presentation.shell;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

/**
 * Diálogo simples "Sobre" (Ajuda).
 */
public final class AboutDialog extends JFrame {

    public AboutDialog() {
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        setTitle("Sobre o Pitiupi");
        setSize(520, 360);
        UiGeometry.centerOnScreen(this);

        JEditorPane texto = new JEditorPane();
        texto.setContentType("text/html");
        texto.setEditable(false);
        texto.setText("""
            <html><body style='font-family:sans-serif'>
            <h2>Pitiupi</h2>
            <p>Plataforma P2P didática com plugins (chat, compartilhamento de tela, exercício ping).</p>
            <p><b>Uso em laboratório:</b> UFSJ / Redes de Computadores.</p>
            </body></html>
            """);

        add(new JScrollPane(texto));
    }
}
