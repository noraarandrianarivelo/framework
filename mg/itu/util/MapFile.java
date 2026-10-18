package mg.itu.util;

public class MapFile {
    private String nom;
    private String extension;
    private byte[] bytes;

    public MapFile(byte[] bytes, String extension, String nom) {
        this.bytes = bytes;
        this.extension = extension;
        this.nom = nom;
    }

    public MapFile() {
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }


}
