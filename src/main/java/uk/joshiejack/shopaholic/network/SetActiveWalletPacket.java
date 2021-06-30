package uk.joshiejack.shopaholic.network;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import uk.joshiejack.penguinlib.util.PenguinLoader;
import uk.joshiejack.shopaholic.Shopaholic;
import uk.joshiejack.shopaholic.api.gold.WalletType;
import uk.joshiejack.shopaholic.client.Wallet;

@PenguinLoader.Packet(NetworkDirection.PLAY_TO_CLIENT)
public class SetActiveWalletPacket extends AbstractSetPlayerNBTPacket {
    private boolean shared;

    public SetActiveWalletPacket() { super(Shopaholic.MODID + "Settings");}
    public SetActiveWalletPacket(boolean shared) {
        super(Shopaholic.MODID + "Settings");
        this.shared = shared;
    }

    @Override
    public void encode(PacketBuffer pb) {
        pb.writeBoolean(shared);
    }

    public void decode(PacketBuffer pb) {
        shared = pb.readBoolean();
    }

    @Override
    public void setData(CompoundNBT tag) {
        tag.putBoolean("SharedWallet", shared);
        Wallet.setActive(shared ? WalletType.SHARED : WalletType.PERSONAL);
    }
}
