import java.io.File;
import java.io.RandomAccessFile;

public class Crud {

  private String nomeDoArquivo = "musicas.db";
  private RandomAccessFile arquivo;

  public Crud() {
    try {
      boolean verificaArquivo = (new File(nomeDoArquivo)).exists();
      if (!verificaArquivo){
        try { // força o primeiro registro a ter id 0
          int id = -1;
          arquivo = new RandomAccessFile(nomeDoArquivo, "rw");
          arquivo.writeInt(id);
          arquivo.close();
        } catch (Exception e){System.out.println(e.getMessage() + "erro em criar o id");}
      }
    }catch(Exception e){System.out.println(e.getMessage());}
  }

  // Método de inserção no arquivo
  public void create(Musica musica) throws Exception {
    arquivo = new RandomAccessFile(nomeDoArquivo, "rw");

    byte[] ba = musica.toByteArray(); // converte musica para byte
    arquivo.seek(0); // move ponteiro para o inicio do arquivo
    arquivo.writeInt(musica.getId()); // escreve id da ultima musica no inicio do arquivo
    arquivo.seek(arquivo.length());// mover para o fim do arquivo

    // escrever registro
    arquivo.writeChar(' '); // escreve a lápide
    arquivo.writeInt(ba.length); // escreve tamanho do registro
    arquivo.write(ba);

    arquivo.close();
    System.out.println("Música adicionada com sucesso! Seu id é " + musica.getId());
  }

  // Método de leitura do arquivo
  public Musica read(int id) throws Exception {
    arquivo = new RandomAccessFile(nomeDoArquivo, "rw");
    byte[] ba;
    int tamanho = arquivo.readInt();
    char lapide = arquivo.readChar();
    Musica musica = new Musica();
    
    arquivo.seek(4); // move ponteiro para o primeiro registro
    while (arquivo.getFilePointer() != arquivo.length()) {
      arquivo.seek(arquivo.getFilePointer() + 1);
      ba = new byte[tamanho];
      arquivo.read(ba);
      if (lapide != '*') {
        musica.fromByteArray(ba); // erro aqui
        if (musica.getId() == id)
          return musica;
      }
    }

    arquivo.close(); 
    return null;
  }

  // Método de inserção no arquivo
  public boolean update(Musica musica) throws Exception {
    arquivo = new RandomAccessFile(nomeDoArquivo, "rw");
    byte[] ba;
    byte[] newBa;
    int tamanho;
    char lapide = arquivo.readChar();
    long posicao;
    Musica musicaArq = new Musica();

    arquivo.seek(4); // move ponteiro para o primeiro registro
    while (arquivo.getFilePointer() != arquivo.length()) {
      posicao = arquivo.getFilePointer(); // posicao atual do ponteiro no arquivo
      tamanho = arquivo.readInt();
      ba = new byte[tamanho];
      arquivo.read(ba);
      if (lapide != '*') {
        musicaArq.fromByteArray(ba); // le a musica do arquivo
        if (musicaArq.getId() == musica.getId()) {
          newBa = musica.toByteArray();
          if (newBa.length <= tamanho) { // se for menor que o registro anterior, sobrescreve
              arquivo.seek(posicao + 6);
              arquivo.write(newBa);

              arquivo.close();
              return true;
          } else { // senao, escreve no fim do arquivo e deleta o anterior
              arquivo.seek(arquivo.length());
              arquivo.writeChar(' ');
              arquivo.writeInt(newBa.length);
              arquivo.write(newBa);
              delete(musicaArq.getId());

              arquivo.close();
              return true;
          }
        }
      }
    }
    arquivo.close();
    return false;
  }

  // Método de exclusão do arquivo
  public Musica delete(int id) throws Exception {
    arquivo = new RandomAccessFile(nomeDoArquivo, "rw");
    byte[] ba;
    int tamanho;
    char lapide = arquivo.readChar();
    long posicao;
    Musica musica = new Musica();

    arquivo.seek(4); // move ponteiro para o primeiro registro
    while (arquivo.getFilePointer() != arquivo.length()) {
      posicao = arquivo.getFilePointer(); // posicao atual do ponteiro no arquivo
      tamanho = arquivo.readInt();
      ba = new byte[tamanho];
      arquivo.read(ba);
      if (lapide != '*') {
        musica.fromByteArray(ba);
        if (musica.getId() == id){
          arquivo.seek(posicao); // volta para a posicao inicial do registro
          arquivo.writeChar('*'); // marca a lapide
          arquivo.close();
          return musica;
        }
      }
    }

    arquivo.close();
    return null;
  }

}
