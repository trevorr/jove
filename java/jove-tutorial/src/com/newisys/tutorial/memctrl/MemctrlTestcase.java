package com.newisys.tutorial.memctrl;

import com.newisys.dv.DVApplication;
import com.newisys.dv.DVSimulation;
import com.newisys.verilog.EdgeSet;
import com.newisys.verilog.util.BitVector;
import com.newisys.verilog.util.Bit;

public final class MemctrlTestcase
    extends DVApplication
{

    public MemctrlTestcase(DVSimulation dvSim)
    {
        super(dvSim);
    }

    public void run()
    {
        MemctrlPort bind = MemctrlBind.INSTANCE;

        // write 0x4E to address 0xDD
        bind.rw.drive(0); // drive R/W low to initiate a write
        bind.addr.drive(0xDD); // write to address 0x77
        bind.dataout.drive(0x4E); // write the value 0x4E

        // advance to the next clock and write 0x22 to addr 0xDC
        bind.clk.syncEdge(EdgeSet.POSEDGE);
        bind.addr.drive(0xDC); // write to address 0xDC
        bind.dataout.drive(0x22); // write the value 0x22

        // advance to the next clock and read addr 0xDD
        bind.clk.syncEdge(EdgeSet.POSEDGE);

        bind.rw.drive(1); // drive R/W high to initiate a read
        bind.addr.drive(0xDD); // read from address 0x77

        // due to -1 skew, we wait two cycles before sampling dataout
        bind.clk.syncEdge(EdgeSet.POSEDGE);
        bind.clk.syncEdge(EdgeSet.POSEDGE);
        final BitVector value = bind.datain.sample();

        if (value.intValue() != 0x4E)
        {
            throw new AssertionError("Bad data. Expected: 8'h4e, Actual: "
                + value);
        }
    }
}
