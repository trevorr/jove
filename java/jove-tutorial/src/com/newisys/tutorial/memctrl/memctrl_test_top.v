module memctrl_test_top;
    reg SystemClock;
    wire[7:0] addr;
    wire[7:0] datain;
    wire[7:0] dataout;
    wire rw;
    wire clk;
    
    assign clk = SystemClock;

    MemctrlIfgenTB vshell();
    
    initial
    begin
        SystemClock = 0;
        
        forever
        begin
            #100 SystemClock = ~SystemClock;
        end
    end
    
    memory dut(clk, rw, addr, datain, dataout);
endmodule

